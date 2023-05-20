from PIL import Image
import numpy as np
import sys
import os
import cv2

def resize_img( file_path ) :

    if file_path.endswith( '.png' ) or file_path.endswith( '.jpg' ) or file_path.endswith( '.jpeg' ):
        print( 'Resizing ' + file_path + '...' )
        img = cv2.imread(file_path)
        img = cv2.resize(img, (450, 600), interpolation=cv2.INTER_AREA)
                    
        try:
            os.mkdir( dir )
        except:
            pass
            cv2.imwrite(file_path, img)

resize_img( sys.argv[1])