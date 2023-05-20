import os 
os.environ["CUDA_VISIBLE_DEVICES"] = '-1'

import os,time,cv2, sys, math
import tensorflow.compat.v1 as tf
import argparse
import numpy as np
import pyscreenshot as ImageGrab

from utils import utils, helpers
from builders import model_builder

tf.compat.v1.disable_eager_execution()
parser = argparse.ArgumentParser()
parser.add_argument('--image', type=str, default='output/output.jpg', required=False, help='The image you want to predict on. ')
parser.add_argument('--checkpoint_path', type=str, default='BDD-512-640/Checkpoint/latest_model_FC-DenseNet103_CamVid.ckpt', required=False, help='The path to the latest checkpoint weights for your model.')
parser.add_argument('--crop_height', type=int, default=512, help='Height of cropped input image to network')
parser.add_argument('--crop_width', type=int, default=640, help='Width of cropped input image to network')
parser.add_argument('--model', type=str, default='FC-DenseNet103', required=False, help='The model you are using')
parser.add_argument('--dataset', type=str, default="CamVid", required=False, help='The dataset you are using')
args = parser.parse_args()

class_names_list, label_values = helpers.get_label_info(os.path.join(args.dataset, "class_dict.csv"))

num_classes = len(label_values)

print("\n***** Begin prediction *****")
print("Dataset -->", args.dataset)
print("Model -->", args.model)
print("Crop Height -->", args.crop_height)
print("Crop Width -->", args.crop_width)
print("Num Classes -->", num_classes)
print("Image -->", args.image)

# Initializing network
config = tf.ConfigProto()
#config.gpu_options.allow_growth = True
sess=tf.Session(config=config)

net_input = tf.placeholder(tf.float32,shape=[None,None,None,3])
net_output = tf.placeholder(tf.float32,shape=[None,None,None,num_classes]) 

network, _ = model_builder.build_model(args.model, net_input=net_input,
                                        num_classes=num_classes,
                                        crop_width=args.crop_width,
                                        crop_height=args.crop_height,
                                        is_training=False)

sess.run(tf.global_variables_initializer())

print('Loading model checkpoint weights')
saver=tf.train.Saver(max_to_keep=1000)
saver.restore(sess, args.checkpoint_path)

def predict() :
    print("Testing image " + args.image)

    loaded_image = utils.load_image(args.image)
    resized_image =cv2.resize(loaded_image, (args.crop_width, args.crop_height))
    input_image = np.expand_dims(np.float32(resized_image[:args.crop_height, :args.crop_width]),axis=0)/255.0

    st = time.time()
    output_image = sess.run(network,feed_dict={net_input:input_image})

    run_time = time.time()-st

    output_image = np.array(output_image[0,:,:,:])
    output_image = helpers.reverse_one_hot(output_image)

    out_vis_image = helpers.colour_code_segmentation(output_image, label_values)
    file_name = utils.filepath_to_name(args.image)
    cv2.imwrite("output/%s_pred.png"%(file_name),cv2.cvtColor(np.uint8(out_vis_image), cv2.COLOR_RGB2BGR))

    print("")
    print("Finished!")
    print("Wrote image " + "%s_pred.png"%(file_name))

cap = cv2.VideoCapture(0)
if not cap.isOpened():
    print("Cannot open camera")
    exit()

import time
start_time = time.time()
counter = 0 

import client
import numpy

while True:
    try :
        img = ImageGrab.grab().convert('RGB')  # X1, Y1, X2, Y2
        img.save("output/output.jpg" )
        counter += 1
        print("FPS: ", counter/(time.time()-start_time))
        counter = 0

        #img2 = cv2.cvtColor(numpy.asarray(img), cv2.COLOR_RGB2BGR)
        #cv2.imshow('real-time', img2)     # 如果讀取成功，顯示該幀的畫面

        predict()
        #client.sendimg('output/output.jpg')
        #client.sendimg('output/output_pred.jpg')

    except KeyboardInterrupt :
        break

cv2.destroyAllWindows()                 # 結束所有視窗

'''
while True:
    ret, frame = cap.read()             # 讀取影片的每一幀
    if not ret:
        print("Cannot receive frame")   # 如果讀取錯誤，印出訊息
        break
    counter += 1
    print("FPS: ", counter/(time.time()-start_time))
    counter = 0
    cv2.imshow('real-time', frame)     # 如果讀取成功，顯示該幀的畫面
    cv2.imwrite('output/output.jpg', frame )

    predict()
    client.sendimg('output/output.jpg')
    client.sendimg('output/output_pred.jpg')

    if cv2.waitKey(1) == ord('q'):      # 每一毫秒更新一次，直到按下 q 結束
        break
cap.release()                           # 所有作業都完成後，釋放資源
cv2.destroyAllWindows()                 # 結束所有視窗
'''