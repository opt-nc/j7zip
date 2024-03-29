[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)
![Build](https://github.com/opt-nc/j7zip/actions/workflows/test-release.yml/badge.svg)
![CodeQL](https://github.com/opt-nc/j7zip/actions/workflows/codeql-analysis.yml/badge.svg)

# ❔ About

`j7zip` is Command line tool to compress / decompress files using [LZMA](https://www.7-zip.org/sdk.html)
algortihm (like [7-Zip](https://www.7-zip.org) for .7z) with password protection, same like `p7zip` 
but in Java 8 ! :drum: _(for compatibility with AS400 :vhs:)_



# :pager: Usage 

After downloading, get the lastest help usage typing :

```shell
java -jar j7zip.jar -h
```

#  🕹️ Give `j7zip` a try

A [dedicated Killecoda scenario](https://killercoda.com/opt-labs/course/devops-tools/j7zip) has
been created so you can give it a try **from your browser**.


# 👴 Supported systems

Below a list of systems (add your's) on which we have installed and tested `j7zip` (the older the better 😅) :

| Operating System     | Java runtime                   |
| -------------------- | ------------------------------ |
| `Ubuntu 20.04.5 LTS` | `openjdk version "1.8.0_302"`  |


# 🙏 Credits

This tool is based on:

- [Apache commons-compress](https://commons.apache.org/proper/commons-compress/)
- [xz](https://tukaani.org/xz/java.html)

# 💡 Related softwares

-  [`NanaZip`](https://github.com/M2Team/NanaZip) (_"The 7-Zip derivative intended for the modern Windows experience "_)
