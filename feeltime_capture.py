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

cv2.namedWindow("preview", flags=cv2.WINDOW_NORMAL)
cv2.namedWindow("request", flags=cv2.WINDOW_NORMAL)
vc = cv2.VideoCapture(1)

if vc.isOpened(): # try to get the first frame
    rval, frame = vc.read()
else:
    rval = False

while True:
  flag = db.request.find_one()['value']
  if not flag:
    time.sleep(1)
  else:
      cv2.imshow("preview", frame)
      rval, frame = vc.read()
      key = cv2.waitKey(20)
      if key == 13: # on enter
        cv2.imwrite("tmp_image.jpg", frame)
        image = open("tmp_image.jpg", "rb")
        faces = CF.face.detect(image, attributes="emotion")
        image.close()
        for face in faces:
          # r = face['faceRectangle']
          # cv2.rectangle(frame, (r['left'], r['top']),
          #     (r['left'] + r['width'], r['top'] + r['height']),
          #     (255,0,0),
          #     3)
          # yidx = 1
          maxem = ""
          maxval = 0.0
          for em, val in face['faceAttributes']['emotion'].items():
            if val > maxval and (em != 'neutral' or val > 0.9):
              maxval = val
              maxem = em
            # cv2.putText(frame, "%s: %f" % (em.capitalize(), val),
            #     (0, yidx * 32),
            #     cv2.FONT_HERSHEY_PLAIN,
            #     2.0,
            #     (0, 0, 255))
            # yidx += 1
          db.display1.update({}, {"Emotion":maxem, "Value":maxval}, upsert=True)
        cv2.imshow("request", frame)
        print(faces)
      if key == 27: # exit on ESC
          break

vc.release()
cv2.destroyWindow("preview")
cv2.destroyWindow("request")
