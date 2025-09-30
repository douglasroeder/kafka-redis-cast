terraform {
  required_version = ">= 1.0"
  required_providers {
    # Add providers you'll use, e.g.:
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Configure your provider
provider "aws" {
  region = var.aws_region
}