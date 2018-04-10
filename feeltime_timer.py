import cv2
import cognitive_face as CF
import config as env
import pymongo
import time

from pymongo import MongoClient
client = MongoClient('localhost', 27017)
db = client['feeltime']

FACE_API_KEY = env.FACE_API_KEY # defined in config.py
CF.Key.set(FACE_API_KEY)

BASE_URL = 'https://westcentralus.api.cognitive.microsoft.com/face/v1.0/'  # Replace with your regional Base URL
CF.BaseUrl.set(BASE_URL)

vc = cv2.VideoCapture(0)

if vc.isOpened(): # try to get the first frame
    rval, frame = vc.read()
else:
    rval = False

while rval:
    rval, frame = vc.read()
    cv2.imwrite("tmp_image.jpg", frame)
    image = open("tmp_image.jpg", "rb")
    faces = CF.face.detect(image, attributes="emotion")
    print(faces)
    image.close()
    for face in faces:
      maxem = ""
      maxval = 0.0
      for em, val in face['faceAttributes']['emotion'].items():
        if val > maxval and (em != 'neutral' or val > 0.9):
          if em == "contempt":
            maxem = "anger"
          else:
            maxem = em
          maxval = val
      db.display1.update({}, {"Emotion":maxem, "Value":maxval}, upsert=True)
    time.sleep(5)
vc.release()
