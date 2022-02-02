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



def parse_directory(directory):
    size = 0
    for root, dirs, files in os.walk(directory):
        for file in files:
            size += os.path.getsize(os.path.join(root, file))
    return size

def delete_file(bucket, key):
    try:
#        print("Debug: Deleting file: {0} from {1}".format(key, input_bucket))
#        cos.delete_object(Bucket=bucket, Key=key)
        cos.Object(bucket,key).delete()
    except Exception as e:
        print(Exception, e)
#     else:
#         print('{0} Deleted'.format(key))

def download_file(input_bucket, key, download_path):
    try:
        actualdownloadpath=download_path+"/"+key
        downloaddir=os.path.dirname(actualdownloadpath)
#         print("Debug: Downloading file: {0} from {1} to {2}".format(key, input_bucket, actualdownloadpath))
        if not os.path.exists(downloaddir):
            os.makedirs(downloaddir)
        res=cos.Object(input_bucket, key).download_file(actualdownloadpath)
    except Exception as e:
        print(Exception, e)

def download_directory(input_bucket, key, download_path):
    try:
        files = cos.Bucket(input_bucket).objects.all()
        for file in files:
            if (file.key.startswith(key)):
#                 print("Debug: Identfied file: {0} ({1} bytes).".format(file.key, file.size))
                download_file(input_bucket, file.key, download_path)
    except ClientError as be:
        print("CLIENT ERROR: {0}\n".format(be))
    except Exception as e:
        print("Unable to retrieve bucket contents: {0}".format(e))



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
    downloadpath = '/tmp/{}-{}'.format(key, salt)
    os.makedirs(downloadpath)

    s3_download_begin = datetime.datetime.now()
#     print("Debug: Downloading directory {0}".format(key))
    download_directory(input_bucket, key, downloadpath)
#    r = requests.get(url,allow_redirects=True)
#    open(downloadpath+"/"+"testfile", 'wb').write(r.content)
    s3_download_stop = datetime.datetime.now()
    size = parse_directory(downloadpath)

    compress_begin = datetime.datetime.now()
    shutil.make_archive(os.path.join(downloadpath, key), 'zip', root_dir=downloadpath)
    compress_end = datetime.datetime.now()

    s3_upload_begin = datetime.datetime.now()
    archive_name = '{}-{}.zip'.format(key,salt)
    local_name = '{}.zip'.format(key)
    archive_size = os.path.getsize(os.path.join(downloadpath, local_name))
#     print("Debug: Uploading directory {0}".format(archive_name))
    upload(output_bucket, archive_name, os.path.join(downloadpath, local_name))
    s3_upload_stop = datetime.datetime.now()

    download_time = (s3_download_stop - s3_download_begin)
    upload_time = (s3_upload_stop - s3_upload_begin)
    process_time = (compress_end - compress_begin)

#    print ('download_time (s): ' + "%.03f"%(download_time.seconds+download_time.microseconds/1000000.0))
#    print ('download_size: ' + str(size))
#    print ('compute_time (s): ' + "%.03f"%(process_time.seconds+process_time.microseconds/1000000.0))
#    print ('upload_size: ' + str(archive_size))
#    print ('upload_time (s): ' + "%.03f"%(upload_time.seconds+upload_time.microseconds/1000000.0))
    retval = "###pcompress, "+"%.03f"%(download_time.seconds+download_time.microseconds/1000000.0) + ", "+ \
        str(size) + ", "+ "%.03f"%(process_time.seconds+process_time.microseconds/1000000.0) + ", "+ \
        str(archive_size) + ", "+ "%.03f"%(upload_time.seconds+upload_time.microseconds/1000000.0)

    delete_file(output_bucket, archive_name)

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
