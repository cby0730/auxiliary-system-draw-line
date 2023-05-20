import numpy as np
import cv2
from keras.models import load_model

def test_an_image(file_path, model):
    """
    resize the input image to [32, 32, 3], then feed it into the NN for prediction
    :param file_path:
    :return:
    """

    desired_dim=(32,32)
    img = cv2.imread(file_path)
    img_resized = cv2.resize(img, desired_dim, interpolation=cv2.INTER_LINEAR)
    img_ = np.expand_dims(np.array(img_resized), axis=0)

    #predicted_state = model.predict_classes(img_)

    predict_x=model.predict(img_) 
    predicted_state=np.argmax(predict_x,axis=1)

    return predicted_state

def detect_traffic_light( file_path ) :

    states = ['red', 'green', 'off']
    predicted_state = test_an_image(file_path, model=load_model('smallVGG.h5'))
    return states[predicted_state[0]]
