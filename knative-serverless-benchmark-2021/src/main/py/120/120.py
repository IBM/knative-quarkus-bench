#!/bin/python3

import requests
import datetime
import io
import os
import shutil
import uuid
import zlib
import json
import sys

import ibm_boto3
from ibm_botocore.client import Config, ClientError

try:
    with open (os.environ['HOME']+"/.bluemix/cos_credentials","r") as jsonfile:
        configs = json.load(jsonfile)
        configapikey=configs["apikey"]
        configrinstance=configs["resource_instance_id"]
except Exception as e:
    configapikey="notset"
    configrinstance="notset"

# obtain IBM COS values from either config file or environment variables
# environment variables have priority
COS_ENDPOINT = os.environ.get("COS_ENDPOINT", "https://s3.direct.us-south.cloud-object-storage.appdomain.cloud")
COS_APIKEY = os.environ.get("COS_APIKEY", configapikey)
COS_INSTANCE_CRN = os.environ.get("COS_INSTANCE_CRN", configrinstance)
COS_IN_BUCKET = os.environ.get("COS_IN_BUCKET", "trl-knative-benchmark-bucket-1")
COS_OUT_BUCKET = os.environ.get("COS_OUT_BUCKET", "trl-knative-benchmark-bucket-2")


# Create resource
cos = ibm_boto3.resource("s3",
    ibm_api_key_id=COS_APIKEY,
    ibm_service_instance_id=COS_INSTANCE_CRN,
    config=Config(signature_version="oauth"),
    endpoint_url=COS_ENDPOINT
)



def filesize(path):
    size = os.path.getsize(path)
    return size

def delete_file(bucket, key):
    try:
        cos.Object(bucket,key).delete()
    except Exception as e:
        print(Exception, e)

def download_file(input_bucket, key, downloadpath):
    try:
        res=cos.Object(input_bucket, key).download_file(downloadpath)
    except Exception as e:
        print(Exception, e)

def upload(bucket_name, archive_name, upload_path):
#    print ("Debug: Uploading {0} to {1} from {2}.".format(archive_name, bucket_name, upload_path))
    try:
        obj = cos.Bucket(bucket_name).Object(archive_name)
        with open (upload_path, 'rb') as data:
            obj.upload_fileobj(data)
#        print("Debug: Item: {0} uploaded!".format(archive_name))
    except ClientError as be:
        print("CLIENT ERROR: {0}\n".format(be))
    except Exception as e:
        print("Unable to create file: {0}".format(e))



def handler(input_bucket,output_bucket,key):
  
    salt = uuid.uuid4()
    downloadpath = '/tmp/120-{}.txt'.format(salt)

    download_begin = datetime.datetime.now()
    download_file(input_bucket, key+"/yes.txt", downloadpath)
    download_stop = datetime.datetime.now()
    size = filesize(downloadpath)

    upload_begin = datetime.datetime.now()
    upload(output_bucket, downloadpath, downloadpath)
    upload_stop = datetime.datetime.now()

    download_time = (download_stop - download_begin)
    upload_time = (upload_stop - upload_begin)

#    print ('download_time (s): ' + "%.03f"%(download_time.seconds+download_time.microseconds/1000000.0))
#    print ('download_size: ' + str(size))
#    print ('upload_time (s): ' + "%.03f"%(upload_time.seconds+upload_time.microseconds/1000000.0))
    retval = "###p120, "+"%.03f"%(download_time.seconds+download_time.microseconds/1000000.0) + ", "+ \
        str(size) + ", "+ \
        "%.03f"%(upload_time.seconds+upload_time.microseconds/1000000.0)

    delete_file(output_bucket,downloadpath)

    return (retval)


# key is name of directory to download.
# handler(COS_IN_BUCKET,COS_OUT_BUCKET,"small")
# print ('')
# handler(COS_IN_BUCKET,COS_OUT_BUCKET,"medium")
# print ('')
datasize="large"
if len(sys.argv)>1:
    datasize=sys.argv[1]
retval = handler(COS_IN_BUCKET,COS_OUT_BUCKET,datasize)
print (retval)
