# xnav

Window manager-like key bindings for (X11-based) desktop environments.

## Requirements

* [xdotool](https://github.com/jordansissel/xdotool)

## Usage

The easiest way to run xnav is to download the binary under Releases then run:

```shell
$ xnav --help
```

To run it from source (requires clojure):

```shell
$ bin/app --help
```

## Develop

To start developing in a REPL:

```shell
$ bin/dev
```

To build as a stand-alone binary using GraalVM:

```shell
$ bin/graalify
```

