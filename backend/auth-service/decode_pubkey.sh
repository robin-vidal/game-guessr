#!/bin/sh

# Utility to show decoded public key from environment, useful for validating signatures manually
echo "$RSA_PUBLIC_KEY" | fold -w 64 | \
  sed '1i-----BEGIN PUBLIC KEY-----' | \
  sed '$a-----END PUBLIC KEY-----'
