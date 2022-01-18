#!/bin/python3

import glob, os, json

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



def get_bucket_contents(bucket_name):
    print("Retrieving bucket contents from: {0}".format(bucket_name))
    try:
        files = cos.Bucket(bucket_name).objects.all()
        for file in files:
            print("Item: {0} ({1} bytes).".format(file.key, file.size))
    except ClientError as be:
        print("CLIENT ERROR: {0}\n".format(be))
    except Exception as e:
        print("Unable to retrieve bucket contents: {0}".format(e))

get_bucket_contents("trl-knative-benchmark-bucket")

get_bucket_contents(COS_IN_BUCKET)

get_bucket_contents(COS_OUT_BUCKET)
