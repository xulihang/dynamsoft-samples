#!/usr/bin/env python3
import sys
sys.path.append('ImageTrans_OCR')
import os
import cv2
import time
import datetime
from imagetrans_ocr import ImageTransOCR
import bottle
from bottle import route, run, template, request, static_file
import json
import base64
bottle.BaseRequest.MEMFILE_MAX = 1024 * 1024 # (or whatever you want)

@route('/ocr', method='POST')
def ocr():
    data_type = request.forms.get('data_type')   
    save_path = "./uploaded/"
    if not os.path.exists(save_path):
        os.makedirs(save_path)
    timestamp=str(int(time.time()*1000))
    if data_type=="base64":
        savedName=timestamp+".jpg"
        b64_data = request.forms.get('base64')  
        file_path = "{path}/{file}".format(path=save_path, file=savedName)
        if os.path.exists(file_path)==True:
            os.remove(file_path)   
        f = open(file_path, 'wb')
        f.write(base64.b64decode(b64_data))
        f.close()
    else:        
        upload = request.files.get('upload')   
        name, ext = os.path.splitext(upload.filename)
        print(ext.lower())
        if ext.lower() not in ('.png','.jpg','.jpeg'):
            return "File extension not allowed."            
        savedName=timestamp+ext
        file_path = "{path}/{file}".format(path=save_path, file=savedName)
        if os.path.exists(file_path)==True:
            os.remove(file_path)
        upload.save(file_path) 
        
    recognize_entire_image = request.forms.get('recognize_entire_image')    
    skip_recogniztion = request.forms.get('skip_recogniztion')
    detector_name = request.forms.get('detector')
    recognizer_name = request.forms.get('recognizer')
    lang = request.forms.get('lang')
    if recognize_entire_image=="true":
        recognize_entire_image=True
    else:
        recognize_entire_image=False
    if skip_recogniztion=="true":
        skip_recogniztion=True
    else:
        skip_recogniztion=False
    print(recognize_entire_image)
    

    result = do_ocr(file_path,detector_name,recognizer_name,skip_recogniztion,recognize_entire_image)
    os.remove(file_path)
    return result

def do_ocr(file_path,detector_name,recognizer_name,skip_recogniztion,recognize_entire_image):
    ocr.init_detector(detector_name)
    ocr.init_recognizer(recognizer_name)
    if recognize_entire_image==True:        
        text=ocr.recognize(file_path)
        return text
    else:        
        ret=ocr.do_ocr(file_path,skip_recogniztion)
        return ret 


@route('/batch/read_barcode', method='POST')
def batch_read_barcode():
    save_path = "./uploaded/"
    if not os.path.exists(save_path):
        os.makedirs(save_path)
    timestamp=str(int(time.time()*1000))
    
    upload = request.files.get('upload')   
    name, ext = os.path.splitext(upload.filename)
    print(ext.lower())
    if ext.lower() not in ('.zip'):
        return "Only zip files allowed."            
    savedName=timestamp+ext
    file_path = "{path}/{file}".format(path=save_path, file=savedName)
    if os.path.exists(file_path)==True:
        os.remove(file_path)
    upload.save(file_path) 

    return savedName

@route('/batch/ocr', method='POST')
def batch_ocr():
    save_path = "./uploaded/"
    if not os.path.exists(save_path):
        os.makedirs(save_path)
    timestamp=str(int(time.time()*1000))
    
    upload = request.files.get('upload')   
    name, ext = os.path.splitext(upload.filename)
    print(ext.lower())
    if ext.lower() not in ('.zip'):
        return "Only zip files allowed."            
    savedName=timestamp+ext
    file_path = "{path}/{file}".format(path=save_path, file=savedName)
    if os.path.exists(file_path)==True:
        os.remove(file_path)
    upload.save(file_path) 

    return savedName


@route('/<filepath:path>')
def server_static(filepath):
    return static_file(filepath, root='www')
    
ocr = ImageTransOCR()
run(server="paste",host='0.0.0.0', port=8080)   