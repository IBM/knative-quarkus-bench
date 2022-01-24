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
# When running on local machine, it is perhaps good to source /home/$USER/.env
COS_ENDPOINT = os.environ.get("COS_ENDPOINT", "https://s3.direct.us-south.cloud-object-storage.appdomain.cloud")
COS_APIKEY = os.environ.get("COS_APIKEY", configapikey)
COS_INSTANCE_CRN = os.environ.get("COS_INSTANCE_CRN", configrinstance)
COS_IN_BUCKET = os.environ.get("COS_IN_BUCKET", "trl-knative-benchmark-bucket-1")
COS_OUT_BUCKET = os.environ.get("COS_OUT_BUCKET", "trl-knative-benchmark-bucket-2")


# input is bucket 1


# Create resource
cos = ibm_boto3.resource("s3",
    ibm_api_key_id=COS_APIKEY,
    ibm_service_instance_id=COS_INSTANCE_CRN,
    config=Config(signature_version="oauth"),
    endpoint_url=COS_ENDPOINT
)


def buckets_count():
    return (1, 1)

# bucketidx isn't actually used... better to use bucket name
def upload_func(bucket_idx, key, filepath):
    try:
        with open(filepath, "rb") as f:
            file_content = f.read()
    except Exception as e:
        print("Unable to read filepath: {0}".format(e))
    try:
        cos.Object(COS_IN_BUCKET, key).put(
            Body=file_content
        )
        print("Item: {0} created!".format(key))
    except ClientError as be:
        print("CLIENT ERROR: {0}\n".format(be))
    except Exception as e:
        print("Unable to create file: {0}".format(e))


def upload_files(data_root, data_dir, upload_func):

    for root, dirs, files in os.walk(data_dir):
        prefix = os.path.relpath(root, data_root)
        for file in files:
            file_name = prefix + '/' + file
            filepath = os.path.join(root, file)
            upload_func(0, file_name, filepath)

'''
    Generate test, small and large workload for compression test.

    :param data_dir: directory where benchmark data is placed
    :param bucket: currently not used
    :param upload_func: upload function taking three params(bucket_idx, key, filepath)
'''
def generate_input(data_dir, bucket, upload_func):

    # upload different datasets
    datasets = []
    for dir in os.listdir(data_dir):
        datasets.append(dir)
        upload_files(data_dir, os.path.join(data_dir, dir), upload_func)

# main

generate_input("/tmp/knative-tmp", "trl-knative-benchmark-bucket-1", upload_func)
