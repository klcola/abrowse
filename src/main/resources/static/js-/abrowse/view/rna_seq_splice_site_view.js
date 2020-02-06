ABrowse.view.RNASeqSpliceSiteView = function (genomeBrowser) {
    ABrowse.view.GeneModelLikeView.call(this, genomeBrowser);
    this.minDisplayDepth = 5;
    this.fontHeight = 10;
    //this.halfEntryHeight = 12;
    this.levelHeight = 40;
    //this.spacingBetweenLevel = 2;
};

ABrowse.view.RNASeqSpliceSiteView.prototype = new ABrowse.view.GeneModelLikeView();


ABrowse.view.RNASeqSpliceSiteView.prototype.drawEntry = function (entry, level, trackName) {

    var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;
    var start = entry.left - ORIGINAL_POINT_X;
    var end = entry.right - ORIGINAL_POINT_X;

    var entryAnchorId = ABrowse.view.createEntryAnchorId(trackName, [entry.left, entry.right].join(":"));
    var entryAnchor = document.getElementById(entryAnchorId);
    if (!entryAnchor) {
        entryAnchor = document.createElementNS(ABrowse.SVG_NS, "a");
        entryAnchor.id = entryAnchorId;
        entryAnchor.addEventListener("click", ABrowse.view.GeneModelLikeView.entryClickEventHandler, false);
        entryAnchor.__abrowse__level = level;
        entryAnchor.__abrowse_entry_id = [entry.left, entry.right, entry.depth].join(":");
        //entryAnchor.__abrowse__start = start;
        //entryAnchor.__abrowse__end = end;
        entryAnchor.setAttribute("style", "cursor:pointer;");
        /* this line works for FF17 IE9 */
        entryAnchor.setAttributeNS(ABrowse.XLINK_NS, "style", "cursor:pointer;");
        /* this line and prev line work together for Chrome23 */
    } else {
        return null;
    }

    var x1 = start;
    var x2 = ((start + end) / 2).toFixed(0);
    var x3 = end;

    var y1 = 0;   // level starts from 1
    //var y2 = (level - 1) *  this.levelHeight + 2 * this.halfEntryHeight;
    var y2 = level * this.levelHeight * 2;

    var path = document.createElementNS(ABrowse.SVG_NS, "path");
    var d = ["M", x1, y1, "Q", x2, y2, x3, y1].join(" ");
    path.setAttribute("d", d);
    var color = this.colorSchema["__default"]["splicesite"][level % 3];
    path.setAttribute("stroke", color);
    path.setAttribute("fill", "none");
    path.setAttribute("style", "stroke-width: 2px;");

    var text = document.createElementNS(ABrowse.SVG_NS, "text");
    text.textContent = entry.depth;
    var fontSize = (this.fontHeight / this.genomeBrowser.svgDisplayScale).toFixed(0);

    text.setAttribute("transform", ["scale(1,", this.genomeBrowser.svgDisplayScale, ")"].join(""));
    text.setAttribute("font-size", fontSize + "px");
    text.setAttribute("font-family", "Calibri, Arial, Helvetica, sans-serif");
    text.setAttribute("font-weight", "bold");
    text.setAttribute("fill", color);

    //var textBBox = text.getBBox();
    //var textWidth = textBBox.width;
    //console.log("DEBUG - textWidth:" + textWidth);

    var fontY = ((level *  this.levelHeight) / this.genomeBrowser.svgDisplayScale).toFixed(0);
    var fontX = ((x1 + x3) / 2).toFixed(0);

    text.setAttribute("x", fontX.toString());
    text.setAttribute("y", fontY.toString());

    entryAnchor.appendChild(path);
    entryAnchor.appendChild(text);
    //entryAnchor.__abrowse__max_level = level;
    return entryAnchor;
};

ABrowse.view.RNASeqSpliceSiteView.prototype.drawBlock = function (blockResponse, trackName) {

    //var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;
    var blockSvgGroupId = ABrowse.view.createBlockSvgGroupId(trackName, blockResponse.start, blockResponse.end);
    var blockSvgGroup = document.getElementById(blockSvgGroupId);
    if (!blockSvgGroup) {
        blockSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
        blockSvgGroup.id = blockSvgGroupId;
        blockSvgGroup.__abrowse__start = blockResponse.start;
        blockSvgGroup.__abrowse__end = blockResponse.end;
        blockSvgGroup.__abrowse__max_level = 1;
    } else {
        return null;
    }

    var entries = blockResponse.entryList;
    /* 排序很重要，关系到基因在图中如何排列 */
    entries.sort(function (a, b) {
        return parseFloat(a.left) - parseFloat(b.left);
    });

    for (var idx = 0; idx < entries.length; ++idx) {
        var level = 1;
        var splicesite = entries[idx];
        if (splicesite.depth < this.minDisplayDepth) {
            continue;
        }

        for (var innerIdx = 0; innerIdx < idx; ++innerIdx) {
            var anotherSplicesite = entries[innerIdx];
            if (anotherSplicesite.depth < this.minDisplayDepth) {
                continue;
            }
            if (splicesite.left < anotherSplicesite.right) {
                level = level + 1;
            }
        }

        var entrySvgGroup = this.drawEntry(splicesite, level, trackName);
        if (null != entrySvgGroup) {
            blockSvgGroup.appendChild(entrySvgGroup);
            if (blockSvgGroup.__abrowse__max_level < entrySvgGroup.__abrowse__level) {
                blockSvgGroup.__abrowse__max_level = entrySvgGroup.__abrowse__level;
            }
        }
    }

    return blockSvgGroup;
};
