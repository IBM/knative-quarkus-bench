package com.ibm.trl.funqy.micro;

public class MySocket {

  String request_id;
  String server_address;
  int server_port;
  int repetitions;
  String output_bucket;
  String income_timestamp;

  public String getRequest_id() { return request_id; }
  public void setRequest_id(String request_id) { this.request_id = request_id; }
  public String getServer_address() { return server_address; }
  public void setServer_address(String server_address) { this.server_address = server_address; }
  public int getServer_port() { return server_port; }
  public void setServer_port(int server_port) { this.server_port = server_port; }
  public int getRepetitions() { return repetitions; }
  public void setRepetitions(int repetitions) { this.repetitions = repetitions; }
  public String getOutput_bucket() { return output_bucket; }
  public void setOutput_bucket(String output_bucket) { this.output_bucket = output_bucket; }
  public String getIncome_timestamp() { return income_timestamp; }
  public void setIncome_timestamp(String income_timestamp) { this.income_timestamp = income_timestamp; }
}
