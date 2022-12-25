# 🤓 Linux shell install script

Below are resources to help install `j7zip` on any debian based Linux distro.

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
export J7ZIP_VERSION=v1.1.0

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

git clone https://github.com/opt-nc/j7zip.git
cd j7zip/src/test/resources/

# Compress & protect with password
cp poem.txt poem.txt.org
j7zip --password=mysecret a poem.7z poem.txt
file poem.7z
du -sh * .


# Uncompress with password
j7zip --password=mysecret e poem.7z
md5sum poem.txt*
```
