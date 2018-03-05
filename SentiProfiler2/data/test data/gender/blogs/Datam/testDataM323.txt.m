Did you ever want to resize a video on the fly, scaling it as you would an image? Using intrinsic ratios for video, you can. This technique allows browsers to determine video dimensions based on the width of their containing block. With intrinsic dimensions, a new width triggers a new height calculation, allowing videos to resize and giving them the ability to scale the same way images do. See example one.
The concept

The idea is to create a box with the proper ratio (4:3, 16:9, etc.), then make the video inside that box stretch to fit the dimensions of the box. It’s that simple.
The trick

The padding property is the magic that styles a box with an intrinsic ratio. This is because we’ll set padding in a percentage, based on the width of the containing block.

The CSS rules below illustrate how to style the parent and child to create a “magic wrapper”—a container that proportionally resizes itself depending on the width of its parent. See example two. Let’s review the declarations in each rule, starting with .wrapper-with-intrinsic-ratio.

position: relative
    By declaring position: relative all child elements will position themselves in relation to this container.
padding-bottom: 20%
    This declaration gives the box a specific format. Using 20% for padding makes the height of the box equal to 20% of its width.
    We specifically chose to use padding-bottom rather than padding-top. This is because IE5 removes the “space” created via padding-top from the flow. In other words, using padding-top: 20% would create the layout we want, but the box would act like an absolutely positioned element, overlapping the next elements in the flow.
height: 0
    Specifying a height of 0 gives this element “layout” so that IE5 and IE6 will dimension the inner box properly. To learn more, visit “On having layout.”
    Note: because IE5 and IE6 treat width as a minimum width, you should not use width: 100% as a layout trigger. This causes the box to expand to fill its container rather than respect the width we set for that container. 

Now, let’s consider each declaration within our .element-to-stretch rule. Let's take a closer look at our new selectors and declarations, starting with the #containingBlock selector.

width: 50%
    This is just a wrapper to demonstrate resizing the video based on the viewport width. In the previous example, the containing block was the body element.

Now, let’s examine a couple of the declarations under the .videoWrapper selector.

padding-bottom: 56.25%
    To create a 16:9 ratio, we must divide 9 by 16 (0.5625 or 56.25%).
padding-top: 25px
    To avoid issues with the broken box model (IE5 or IE6 in quirks mode), we use padding-top rather than height to create room for the chrome.

Finally, we use the object, embed selector because, while some browsers rely on object (e.g., Safari), others need embed (e.g., Firefox).

Note: I’m using YouTube’s markup for now, but at the end of this article I’ll be using valid markup and dropping embed.