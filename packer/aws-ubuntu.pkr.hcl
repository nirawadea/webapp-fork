variable "profile" {
  type    = string
  default = "dev"
}

variable "ami_name_prefix" {
  type    = string
  default = "packer-custom-image"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "region" {
  type    = string
  default = "us-east-1"
}

variable "source_ami" {
  type    = string
  default = "ami-0866a3c8686eaeeba"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "vpc_id" {
  type    = string
  description = "The VPC where the Packer build will happen"
  default = "vpc-0f88c8ec4e16ba62e"
}

variable "subnet_id" {
  type    = string
  description = "The subnet within the VPC where the Packer build will happen"
  default = "subnet-0a01bdb10231d7228"
}

variable "artifact_path" {
  type    = string
  description = "Path to the application artifact"
}

variable "DATABASE_ENDPOINT" {
  type    = string
  description = "Database endpoint"
}

variable "DATABASE_NAME" {
  type    = string
  description = "Database name"
}

variable "DB_USERNAME" {
  type    = string
  description = "Database username"
}

variable "DB_PASSWORD" {
  type    = string
  description = "Database password"
}


packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ubuntu" {
  profile                     = var.profile
  ami_name                    = "${var.ami_name_prefix}-{{timestamp}}" # Ensure unique AMI name
  instance_type               = var.instance_type
  region                      = var.region
  associate_public_ip_address = true
  source_ami                  = var.source_ami
  ssh_username = var.ssh_username


 # Specify the VPC and Subnet 
  vpc_id                      = var.vpc_id
  subnet_id                   = var.subnet_id
}

build {
  name = "packer-build"
  sources = [
    "source.amazon-ebs.ubuntu"
  ]

    provisioner "shell" {
      script = "packer/setup.sh"
    }

    provisioner "file" {
       source      = "packer/csye6225.service"
       destination = "/tmp/"
    }

    provisioner "shell" {
       inline = [
         "if [ -d /tmp ]; then echo '/tmp directory exists.'; else echo '/tmp directory does not exist!'; exit 1; fi",
         "if [ -w /tmp ]; then echo '/tmp directory is writable.'; else echo '/tmp directory is not writable!'; exit 1; fi",
         "if [ -f /tmp/csye6225.service ]; then echo 'Service file exists'; else echo 'Service file is missing'; exit 1; fi",
         "chmod 644 /tmp/csye6225.service"
       ]
    }

}