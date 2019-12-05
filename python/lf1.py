import json
import boto3
import urllib.request
import time

from datetime import datetime
from botocore.vendored import requests

def detect_labels(photo, bucket):
    client=boto3.client('rekognition','us-east-1')
    print({'S3Object':{'Bucket':bucket,'Name':photo}})
    response = client.detect_labels(Image={'S3Object':{'Bucket':bucket,'Name':photo}},MaxLabels=10)

    print('Detected labels for ' + photo) 
    res = []
    for label in response['Labels']:
        res.append(label['Name'])
        print ("Label: " + label['Name'])
        print ("Confidence: " + str(label['Confidence']))
        print ("Instances:")
        for instance in label['Instances']:
            print ("  Bounding box")
            print ("    Top: " + str(instance['BoundingBox']['Top']))
            print ("    Left: " + str(instance['BoundingBox']['Left']))
            print ("    Width: " +  str(instance['BoundingBox']['Width']))
            print ("    Height: " +  str(instance['BoundingBox']['Height']))
            print ("  Confidence: " + str(instance['Confidence']))
            print()

        print ("Parents:")
        for parent in label['Parents']:
            print ("   " + parent['Name'])
        print ("----------")
        print ()
    return res
    




def lambda_handler(event, context):
    
    #log s3 trigger
    for k, v in event.items():
        print("k:  ", k, " v: ", v)
    
    #call recognitions
    bucket = event['Records'][0]['s3']['bucket']['name']
    photo = event['Records'][0]['s3']['object']['key']
    
    
    print("bucketname: ", bucket)
    print("photoname: ", photo)
    labels=detect_labels(photo, bucket)

    
    
    
    
    
    #store to ES
    client = boto3.client('es')
    headers = {"Content-Type":"application/json"}
    
    #ES insert
    payload = '{ "index" : { "_index": "restaurants", "_type" : "Restaurant"} }\n'
    payload += '{"objectKey": "'+ photo +'", "bucket": "'+ bucket +'", "createdTimestamp": "'+ datetime.now().strftime("%Y-%m-%dT%H:%M:%S") +'", "labels": '+ json.dumps(labels) +'}\n'
    print("write to es:")
    print(payload)
    
    '''
    payload = {
        "_index": "restaurants",
        "_type": "Restaurant",
        "_source": {
            "objectKey": photo,
            "bucket": bucket,
            "createdTimestamp": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
            "jsp": labels
            }
        }
    '''
    print(str(payload).lower())
    response = requests.put('https://vpc-xl-es-single-iweyxmypvbdfodxpdfmxpy5c6i.us-east-1.es.amazonaws.com/_bulk', data=payload, headers = headers)
    
    
    
    
    #ES search
    
    print(response.json())
    print("error occured when inserting data?: ", response.json()['errors']) # False = inserted
    print("inserted")
    
    
    
    #return
    return {
        'statusCode': 200,
        'body': json.dumps('Photo upload Succeed')
    }

'''
k:   Records  v:  [{'eventVersion': '2.1', 'eventSource': 'aws:s3', 'awsRegion': 'us-east-1', 'eventTime': '2019-12-02T01:10:13.544Z', 'eventName': 'ObjectCreated:Put', 
'userIdentity': {'principalId': 'AWS:AIDAY5DP6SF4FC3MT4WZR'}, 'requestParameters': {'sourceIPAddress': '158.222.173.214'}, 'responseElements': {'x-amz-request-id': 'C24CC6F3E1FB081D', 'x-amz-id-2': 'V++hMdTFMvsWCHIwou2iUSKf7HLbcos1+22P4yqFKyRyTPY8vGV3NEDjR3pbXfZ49U6/jbvAQSk='}, 's3': {'s3SchemaVersion': '1.0', 'configurationId': 'xlEventforPut', 'bucket': {'name': 'photo191130', 'ownerIdentity': {'principalId': 'ANY074M9PXG1Z'}, 'arn': 'arn:aws:s3:::photo191130'}, 'object': {'key': 'panda3.jpg', 'size': 7193, 'eTag': '194777f07bf88cc8311c7fa4f5f787d0', 'sequencer': '005DE464756CB3D339'}}}]
'''
