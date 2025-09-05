from java.lang import Object

from math import sqrt, radians, cos, sin
from pystitch import *
import os
from os.path import join
from base64 import b64encode

def convert(embroidery, file_format="pes"):
    # convert embroidery into PES

    name = embroidery.getName()
    threads = embroidery.getThreads()
    stitches = sum(list(map(lambda x: len(x.getStitches()), threads)))
    print(f"Converting '{name}' with {len(threads)} thread(s) and {stitches} stitch(es) in total.")

    pattern = EmbPattern()

    for thread in threads:
        pattern.add_thread(thread.getColor())
        pattern.color_change(0, 0)

        for stitch in thread.getStitches():
            x = stitch.getX()
            y = stitch.getY()

            if thread.getAbsolute():
                pattern.add_stitch_absolute(STITCH, x,y)
            else:
                pattern.add_stitch_relative(STITCH, x,y)

    pattern.add_stitch_relative(END, 0, 0)
    pattern.move_center_to_origin()

    filename = join(os.environ["HOME"], f"{name}-{stitches}.{file_format}")

    print(f"Saving to '{filename}'.")
    if file_format == "png":
        settings = {"fancy": "true"}
    else:
        settings = None

    write(pattern, filename, settings)
    print("done")

    out = open(filename, "rb")
    result = out.read()
    out.close()

    # todo delete files?
    return result
