[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)
![Build](https://github.com/opt-nc/j7zip/actions/workflows/test-release.yml/badge.svg)
![CodeQL](https://github.com/opt-nc/j7zip/actions/workflows/codeql-analysis.yml/badge.svg)

#  j7zip

# ❔ About

Command line tool to compress / decompress files using [LZMA](https://www.7-zip.org/sdk.html) algortihm (like [7-Zip](https://www.7-zip.org) for .7z) with password protection, same like `p7zip` but in Java 8 ! :drum: _(for compatibility with AS400 :vhs:)_

# :pager: Usage 

After downloading, get the lastest help usage typing :
```shell
java -jar j7zip.jar -h
```

# 🤓 Linux shell install script

Below are resources to help install `j7zip` on any Linux distro.

## ☕ Java runtime (`sdkman!` install)

Java 8 is a prerequisite, here is the script to install it properly:

```
sudo apt-get install zip
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 8.0.302-open
java -version
clear
```


## 📜 Shell Install script


```
# Set up the target version
export J7ZIP_VERSION=v1.0.0

# !!! DO NOT CHANGE ANYTHING BELOW THIS LINE !!!
curl -LO https://github.com/opt-nc/j7zip/releases/download/$J7ZIP_VERSION/j7zip.jar
file j7zip.jar
sudo mkdir /opt/j7zip
sudo cp j7zip.jar /opt/j7zip/j7zip.jar

cat <<EOF >j7zip.sh
#!/bin/sh
java -jar /opt/j7zip/j7zip.jar "\$@"
EOF

sudo cp j7zip.sh /opt/j7zip
sudo chmod +x /opt/j7zip/j7zip.sh
sudo ln -s /opt/j7zip/j7zip.sh /usr/bin/j7zip

# clean up the mess
rm j7zip*
clear
```


## 🚀 Ready to Use `j7zip`

```
# Get the current version
j7zip -V

# Get some help
j7zip -h
```

# 🙏 Credits

This tool is based on:

- [Apache commons-compress](https://commons.apache.org/proper/commons-compress/)
- [xz](https://tukaani.org/xz/java.html)
