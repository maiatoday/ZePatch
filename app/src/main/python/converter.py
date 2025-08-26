#!/usr/bin/env python
#
# start a webserver to convert incoming stitches into a PES file.
#
from java.lang import Object

from math import sqrt, radians, cos, sin
from pystitch import *
import os
from os.path import join
from base64 import b64encode

def convert(stitches):
    # once called, convert stitches into PES or PNG

    # TODO: Read parameter

    pattern = EmbPattern()
    pattern.add_thread(EmbThread(thread=0xFF0000))
    pattern.add_thread(EmbThread(thread=0x00FF00))
    pattern.add_thread(EmbThread(thread=0x0000FF))

    D = 100
    step = 10
    direction = 1

    pattern.add_stitch_absolute(STITCH, 0, 0)
    for _ in range(0, D):
        for __ in range(0, D):
            pattern.add_stitch_relative(STITCH, direction * step, 0)
        pattern.add_stitch_relative(STITCH, 0, step)
        direction *= -1

    # finish last row
    for __ in range(0, D):
        pattern.add_stitch_relative(STITCH, direction * step, 0)

    pattern.color_change(0, 0)

    for i in range(0, 360 + 1, 15):
        alpha = radians(i)
        x = cos(alpha) * D * step / 2 + D / 2 * step
        y = sin(alpha) * D * step / 2 + D / 2 * step

        pattern.add_stitch_absolute(STITCH, x, y)

    pattern.add_stitch_relative(END, 0, 0)
    pattern.move_center_to_origin()

    filename = join(os.environ["HOME"], "generated-in-py.pes")

    print(f"saving to {filename}.")
    write(pattern, filename)
    print("done")

    return open(filename, "rb").read()


def length(x1, y1, x2, y2):
    dx = x2 - x1
    dy = y2 - y1
    l = sqrt(dx * dx + dy * dy)
    return l


def stitch_line(pattern, startx, starty, endx, endy, step):
    line_length = length(startx, starty, endx, endy)
    width = endx - startx
    height = endy - starty

    i = 0.0
    progress = 0.0
    progress_step = 1.0 / line_length

    while i < line_length:
        progress += progress_step

        x = startx + progress * width
        y = starty + progress * height

        pattern.add_stitch_absolute(STITCH, x, y)
        i += step
