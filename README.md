# xnav

Window manager-like key bindings for (X11-based) desktop environments. For now
this only includes navigating workspaces.

## Requirements

* [xdotool](https://github.com/jordansissel/xdotool)

## Usage

The easiest way to run xnav is to download the binary under Releases then run:

```shell
$ xnav --help
```

To run it from source (requires Clojure):

```shell
$ bin/app --help
```

I use [sxhkd](https://github.com/baskerville/sxhkd) to setup global key bindings
like so:

```
# Go to a specific workspace
super + {1-9,0}
  xnav workspace '{1-9,10}'

# Go to the next workspace
super + ctrl + l
  xnav workspace next

# Go to the previous workspace
super + ctrl + h
  xnav workspace previous

# Go to last active workspace
super + grave
  xnav workspace last
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

