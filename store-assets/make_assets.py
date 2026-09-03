#!/usr/bin/env python3
"""Builds every graphic Play Console asks for, from this repository's own sources.

Run from the repository root:

    python3 store-assets/make_assets.py

Nothing here is drawn by hand. The icon is the same three rounded rectangles
`app/src/main/res/drawable/ic_launcher_foreground.xml` declares, at the same
coordinates, on the color `app/src/main/res/values/colors.xml` names. The feature
graphic uses the two fonts in `app/src/main/res/font/` and the short description
`store-assets/LISTING.md` ships. The screenshots are the committed captures with the
phone's own status bar taken off.

That is the point: a graphic drawn by hand is a graphic that stops matching the app the
first time a color moves, and nobody notices because nobody re-renders it.
"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "store-assets"

# app/src/main/res/values/colors.xml, ic_launcher_background.
GROUND = (0x14, 0x1A, 0x2E)
# The mark's own ink, from ic_launcher_foreground.xml.
INK = (0xF3, 0xF1, 0xEC)

# ic_launcher_foreground.xml: three rounded rectangles in a 100 unit space, as
# (x, y, width, height, radius, alpha). Read straight off the pathData.
MARK = [
    (35.5, 14.0, 29.0, 11.0, 5.5, 0.26),
    (28.0, 29.0, 44.0, 12.0, 6.0, 0.50),
    (14.0, 46.0, 72.0, 40.0, 11.0, 1.00),
]
# The vector insets the mark into the adaptive safe zone: scale 0.72, translate 18,
# inside a 108 unit canvas.
SCALE, OFFSET, CANVAS = 0.72, 18.0, 108.0

FONT_SERIF = ROOT / "app/src/main/res/font/newsreader.ttf"
FONT_SANS = ROOT / "app/src/main/res/font/hanken_grotesk.ttf"

# The shipped short description, from LISTING.md. Vocabulary checked there.
TAGLINE = "One active thing per area. Everything else waits."


def draw_mark(canvas: Image.Image, size: int, inset: bool) -> None:
    """The queue mark, scaled to `size`, over whatever `canvas` already holds."""
    unit = size / CANVAS
    for x, y, w, h, r, alpha in MARK:
        if inset:
            px, py = (OFFSET + SCALE * x) * unit, (OFFSET + SCALE * y) * unit
            pw, ph, pr = SCALE * w * unit, SCALE * h * unit, SCALE * r * unit
        else:
            # The mark alone, filling the box, with no adaptive icon safe zone.
            scale = size / 100.0
            px, py, pw, ph, pr = x * scale, y * scale, w * scale, h * scale, r * scale
        # Its own layer, because the three rectangles overlap and compositing them
        # one at a time is what keeps each one's alpha its own.
        layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
        ImageDraw.Draw(layer).rounded_rectangle(
            [px, py, px + pw, py + ph], radius=pr, fill=INK + (round(alpha * 255),)
        )
        canvas.alpha_composite(layer)


def app_icon() -> None:
    """512 by 512, which is what Play asks for. Full bleed; Play rounds it itself."""
    size = 512
    icon = Image.new("RGBA", (size, size), GROUND + (255,))
    draw_mark(icon, size, inset=True)
    icon.save(OUT / "icon-512.png")
    print("icon-512.png            512x512")


def feature_graphic() -> None:
    """1024 by 500. The mark, the name and the shipped short description.

    No claim, no screenshot inside it and no device frame: Play shows this above the
    listing at a size where a phone mockup is unreadable, and section 1 of LISTING.md
    forbids saying anything about a person here as much as anywhere else.
    """
    w, h = 1024, 500
    art = Image.new("RGBA", (w, h), GROUND + (255,))
    draw = ImageDraw.Draw(art)

    name_font = ImageFont.truetype(str(FONT_SERIF), 82)
    tag_font = ImageFont.truetype(str(FONT_SANS), 27)
    name, tag = "Clarity Now", TAGLINE

    # **Measured and centred as one group rather than positioned by eye.** Play crops
    # this graphic's edges on some surfaces, so the whole composition sits inside the
    # middle of the canvas with room on both sides, and the widest of the two lines is
    # what decides how wide the group is.
    mark_size = 196
    gutter = 64
    name_w = draw.textlength(name, font=name_font)
    tag_w = draw.textlength(tag, font=tag_font)
    text_w = max(name_w, tag_w)
    group_w = mark_size + gutter + text_w
    left = (w - group_w) / 2

    mark = Image.new("RGBA", (mark_size, mark_size), (0, 0, 0, 0))
    draw_mark(mark, mark_size, inset=False)
    art.alpha_composite(mark, (round(left), (h - mark_size) // 2))

    text_left = left + mark_size + gutter
    draw.text((text_left, h / 2 - 74), name, font=name_font, fill=INK + (255,))
    draw.text((text_left, h / 2 + 26), tag, font=tag_font, fill=INK + (176,))

    art.convert("RGB").save(OUT / "feature-graphic-1024x500.png")
    print(f"feature-graphic-1024x500.png  {w}x{h}  group {group_w:.0f}px wide, "
          f"{left:.0f}px clear on each side")


def screenshots() -> None:
    """The committed captures, with the phone's own status bar taken off.

    Two things this fixes. The status bar carries the owner's clock, battery and the
    notification icons of every other app on that phone, none of which belongs in a
    store listing. And Play caps a phone screenshot's aspect ratio: these are 1080 by
    2400, which is 2.22 to 1, so the width is padded rather than the height cropped.
    Padding in the page's own ground color is invisible and keeps every pixel of the
    app; cropping to the same ratio would take the tab bar off the bottom.
    """
    src = ROOT / "docs/screenshots"
    dst = OUT / "screenshots"
    dst.mkdir(exist_ok=True)
    # Measured, not guessed: the status bar glyphs occupy rows 50 to 87 in every one
    # of these captures. 100 clears them with a margin and takes no app pixel.
    status_bar = 100
    for path in sorted(src.glob("*.png")):
        shot = Image.open(path).convert("RGB")
        body = shot.crop((0, status_bar, shot.width, shot.height))
        target_w = round(body.height / 2)
        pad = (target_w - body.width) // 2
        ground = body.getpixel((4, 4))
        out = Image.new("RGB", (target_w, body.height), ground)
        out.paste(body, (pad, 0))
        out.save(dst / path.name)
        print(f"screenshots/{path.name:22} {out.width}x{out.height}  {out.height / out.width:.3f}:1")


if __name__ == "__main__":
    app_icon()
    feature_graphic()
    screenshots()
