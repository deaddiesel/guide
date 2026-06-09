package com.deaddiesel.mods.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnimatedGif {
    private final List<BufferedImage> rawFrames = new ArrayList<>();
    private final List<Integer> delays = new ArrayList<>();
    private final List<Integer> lefts = new ArrayList<>();
    private final List<Integer> tops = new ArrayList<>();
    private final List<Integer> widths = new ArrayList<>();
    private final List<Integer> heights = new ArrayList<>();

    private int currentFrame = 0;
    private long lastTime = 0;
    private boolean loaded = false;
    private int gifWidth = 0;
    private int gifHeight = 0;

    private BufferedImage compositedCanvas;
    private Graphics2D g2d;

    private BufferedImage scratchBuffer;
    private Graphics2D scratchG2d;

    public void load(ResourceLocation location) {
        Resource resource = Minecraft.getInstance().getResourceManager()
                .getResource(location).orElse(null);

        if (resource == null) {
            GuideMod.LOGGER.error("GIF not found: {}", location);
            return;
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(resource.open())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                GuideMod.LOGGER.error("No GIF reader available");
                return;
            }

            ImageReader reader = readers.next();
            reader.setInput(input);
            int frameCount = reader.getNumImages(true);

            GuideMod.LOGGER.info("=== Loading GIF: {} ===", location);

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = reader.read(i);
                rawFrames.add(frame);

                int delay = 100, left = 0, top = 0, w = frame.getWidth(), h = frame.getHeight();

                try {
                    IIOMetadata meta = reader.getImageMetadata(i);
                    var tree = meta.getAsTree(meta.getNativeMetadataFormatName());
                    if (tree instanceof org.w3c.dom.Element el) {
                        var gce = el.getElementsByTagName("GraphicControlExtension");
                        if (gce.getLength() > 0) {
                            var gceEl = (org.w3c.dom.Element) gce.item(0);
                            String delayStr = gceEl.getAttribute("delayTime");
                            if (!delayStr.isEmpty()) delay = Integer.parseInt(delayStr) * 10;
                        }
                        var imgDesc = el.getElementsByTagName("ImageDescriptor");
                        if (imgDesc.getLength() > 0) {
                            var imgEl = (org.w3c.dom.Element) imgDesc.item(0);
                            String leftStr = imgEl.getAttribute("imageLeftPosition");
                            String topStr = imgEl.getAttribute("imageTopPosition");
                            String wStr = imgEl.getAttribute("imageWidth");
                            String hStr = imgEl.getAttribute("imageHeight");

                            if (!leftStr.isEmpty()) left = Integer.parseInt(leftStr);
                            if (!topStr.isEmpty()) top = Integer.parseInt(topStr);
                            if (!wStr.isEmpty()) w = Integer.parseInt(wStr);
                            if (!hStr.isEmpty()) h = Integer.parseInt(hStr);
                        }
                    }
                } catch (Exception ignored) {}

                if (w == 0) w = frame.getWidth();
                if (h == 0) h = frame.getHeight();

                delays.add(delay);
                lefts.add(left);
                tops.add(top);
                widths.add(w);
                heights.add(h);
            }
            reader.dispose();

            gifWidth = rawFrames.get(0).getWidth();
            gifHeight = rawFrames.get(0).getHeight();

            compositedCanvas = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_ARGB);
            g2d = compositedCanvas.createGraphics();
            g2d.setComposite(AlphaComposite.SrcOver);

            scratchBuffer = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_ARGB);
            scratchG2d = scratchBuffer.createGraphics();
            scratchG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            scratchG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            rebuildCurrentFrame();

            loaded = true;
            GuideMod.LOGGER.info("Loaded GIF: {} ({}x{}, {} frames)", location, gifWidth, gifHeight, frameCount);

        } catch (IOException e) {
            GuideMod.LOGGER.error("Failed to read GIF: {}", location, e);
        }
    }

    public void tick() {
        if (!loaded) return;
        long now = System.currentTimeMillis();
        if (now - lastTime >= delays.get(currentFrame)) {
            currentFrame = (currentFrame + 1) % rawFrames.size();
            lastTime = now;
            rebuildCurrentFrame();
        }
    }

    private void rebuildCurrentFrame() {
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, gifWidth, gifHeight);

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(rawFrames.get(currentFrame), lefts.get(currentFrame), tops.get(currentFrame), null);
    }

    public BufferedImage getCurrentFrame() { return compositedCanvas; }
    public boolean isLoaded() { return loaded; }

    @SuppressWarnings("unused")
    public int getWidth() { return gifWidth; }

    @SuppressWarnings("unused")
    public int getHeight() { return gifHeight; }

    public void clear() {
        rawFrames.clear();
        delays.clear();
        lefts.clear();
        tops.clear();
        widths.clear();
        heights.clear();

        if (g2d != null) {
            g2d.dispose();
            g2d = null;
        }
        if (scratchG2d != null) {
            scratchG2d.dispose();
            scratchG2d = null;
        }

        if (compositedCanvas != null) {
            compositedCanvas.flush();
            compositedCanvas = null;
        }
        if (scratchBuffer != null) {
            scratchBuffer.flush();
            scratchBuffer = null;
        }

        currentFrame = 0;
        loaded = false;
    }
}