ABrowse.view.GeneModelLikeView = function (genomeBrowser) {
    ABrowse.view.View.call(this, genomeBrowser);
    /*
    this.fontHeight = 10;
    this.textGraphSpacing = 2;
    this.halfEntryHeight = 5;
    this.halfIntronHeight = 1;
    this.levelHeight = 25;
    */
    this.fontHeight = 8;
    this.textGraphSpacing = 1;
    this.halfEntryHeight = 3;
    this.halfIntronHeight = 1;
    this.levelHeight = 15;
    this.spacingBetweenLevel = 2;
};

ABrowse.view.GeneModelLikeView.entryClickEventHandler = function (event) {
    var entryAnchor = event.target;
    //console.log("DEBUG - event.target: " + entryAnchor.innerHTML + ", " + entryAnchor.parentNode.__abrowse__level);
    window.alert("clicked:" + entryAnchor.parentNode.__abrowse_entry_id);
};

ABrowse.view.GeneModelLikeView.prototype = new ABrowse.view.View();

ABrowse.view.GeneModelLikeView.prototype.applyColorSchema = function (colorSchema) {

    if (colorSchema) {
        this.colorSchema = colorSchema;
    }
};

ABrowse.view.GeneModelLikeView.prototype.drawTranscript = function (transcript, level, trackName) {
    var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;  // input start
    var entryId = transcript.transcript_id;
    var transcriptName = transcript.attributes.transcript_name;

    var start = transcript.start - ORIGINAL_POINT_X;
    var end = transcript.end - ORIGINAL_POINT_X;

    var entryAnchorId = ABrowse.view.createEntryAnchorId(trackName, entryId);
    var entryAnchor = document.getElementById(entryAnchorId);

    if (!entryAnchor) {
        entryAnchor = document.createElementNS(ABrowse.SVG_NS, "a");
        entryAnchor.id = entryAnchorId;
        entryAnchor.addEventListener("click", ABrowse.view.GeneModelLikeView.entryClickEventHandler, false);
        entryAnchor.__abrowse__level = level;
        entryAnchor.__abrowse_entry_id = entryId;
        entryAnchor.setAttribute("style", "cursor:pointer;");
        /* this line works for FF17 IE9 */
        entryAnchor.setAttributeNS(ABrowse.XLINK_NS, "style", "cursor:pointer;");
        /* this line and prev line work together for Chrome23 */
    } else {
        return null;
    }

    var fontHeight = this.fontHeight;

    var y = (level - 1) * ( this.levelHeight + this.spacingBetweenLevel);   // level starts from 1
    var pointsArray = [];
    var y_dist = this.halfEntryHeight - this.halfIntronHeight;
    var blocks = transcript.blocks;
    //if (transcript.strand == "-") {
    blocks.sort(function (a, b) {
        return parseFloat(a.start) - parseFloat(b.start);
    });
    //}
    for (var idx = 0; idx < blocks.length; ++idx) {

        var block = blocks[idx];
        if (block.feature != "exon") {
            continue;
        }

        var s = block.start - ORIGINAL_POINT_X;
        var e = block.end - ORIGINAL_POINT_X;

        var x1 = s;
        var y1 = y + y_dist + fontHeight + this.textGraphSpacing;
        var x2 = s;
        var y2 = y + fontHeight + this.textGraphSpacing;
        var x3 = e - 1;
        var y3 = y2;
        var x4 = e - 1;
        var y4 = y1;

        pointsArray.push([x1, y1].join(","));
        pointsArray.push([x2, y2].join(","));
        pointsArray.push([x3, y3].join(","));
        pointsArray.push([x4, y4].join(","));
    }

    y_dist = this.halfEntryHeight + this.halfIntronHeight;
    for (var idx = blocks.length - 1; idx > -1; --idx) {

        var block = blocks[idx];

        if (block.feature != "exon") {
            continue;
        }

        var s = block.start - ORIGINAL_POINT_X;
        var e = block.end - ORIGINAL_POINT_X;

        var x1 = e - 1;
        var y1 = y + y_dist + fontHeight + this.textGraphSpacing;
        var x2 = e - 1;
        var y2 = y + fontHeight + this.textGraphSpacing + this.halfEntryHeight * 2;
        var x3 = s;
        var y3 = y2;
        var x4 = s;
        var y4 = y1;

        pointsArray.push([x1, y1].join(","));
        pointsArray.push([x2, y2].join(","));
        pointsArray.push([x3, y3].join(","));
        pointsArray.push([x4, y4].join(","));
    }

    var lastPoint = pointsArray[0];
    pointsArray.push(lastPoint);
    var polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
    polyline.setAttribute("points", pointsArray.join(" "));
    polyline.setAttribute("fill", this.colorSchema[transcript.strand]["__default"]);

    var text = document.createElementNS(ABrowse.SVG_NS, "text");
    text.textContent = transcriptName;
    var fontSize = (this.fontHeight / this.genomeBrowser.svgDisplayScale).toFixed(0);
    var fontY = ((y + this.fontHeight) / this.genomeBrowser.svgDisplayScale).toFixed(0);

    text.setAttribute("x", start.toString());
    text.setAttribute("y", fontY);
    text.setAttribute("transform", ["scale(1,", this.genomeBrowser.svgDisplayScale, ")"].join(""));
    text.setAttribute("font-size", fontSize + "px");
    text.setAttribute("font-family", "Calibri, Arial, Helvetica, sans-serif");
    text.setAttribute("font-weight", "bold");
    text.setAttribute("fill", "black");

    entryAnchor.appendChild(text);

    entryAnchor.appendChild(polyline);
    return entryAnchor;

};

ABrowse.view.GeneModelLikeView.prototype.drawEntry = function (gene, level, trackName) {

    var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;
    //var entryAnchors = [];
    var geneSvgGroupId = ABrowse.view.createGeneSvgGroupId(trackName, gene.gene_id);
    var geneSvgGroup = document.getElementById(geneSvgGroupId);

    if (null == geneSvgGroup) {
        geneSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
        geneSvgGroup.id = geneSvgGroupId;

        for (var idx = 0; idx < gene.transcripts.length; ++idx) {

            var entryAnchor = this.drawTranscript(gene.transcripts[idx], level, trackName);
            geneSvgGroup.appendChild(entryAnchor);
            level++;
        }
        geneSvgGroup.__abrowse__max_level = level;
        return geneSvgGroup;
    } else {
        return geneSvgGroup;
        // return null;
    }
};

ABrowse.view.GeneModelLikeView.prototype.drawBlock = function (blockResponse, trackName) {

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
        return parseFloat(a.start) - parseFloat(b.start);
    });
    for (var idx = 0; idx < entries.length; ++idx) {
        var level = 1;
        var gene = entries[idx];

        for (var innerIdx = 0; innerIdx < idx; ++innerIdx) {
            var anotherGene = entries[innerIdx];
            if (gene.start < anotherGene.end) {
                level = level + anotherGene.transcripts.length;
            }
        }

        var geneSvgGroup = this.drawEntry(gene, level, trackName);
        if (null != geneSvgGroup) {
            blockSvgGroup.appendChild(geneSvgGroup);
            if (blockSvgGroup.__abrowse__max_level < geneSvgGroup.__abrowse__max_level) {
                blockSvgGroup.__abrowse__max_level = geneSvgGroup.__abrowse__max_level;
            }
        }

    }
    return blockSvgGroup;
};

ABrowse.view.GeneModelLikeView.prototype.render = function (trackResponse, top) {

    var trackSvgGroupComplexus = this.initializeRender(trackResponse);
    var trackSvgGroup = trackSvgGroupComplexus.trackSvgGroup;
    trackSvgGroup.__abrowse__trackView = this;

    var trackHeaderSvgGroup = trackSvgGroup.childNodes[0];
    var trackBodySvgGroup = trackSvgGroup.childNodes[1];

    var blockResponses = trackResponse.blockResponses;
    for (var j = 0; j < blockResponses.length; ++j) {
        var blockSvgGroup = this.drawBlock(blockResponses[j], trackResponse.trackName);
        if (null != blockSvgGroup) {
            this.insertBlockSvgGroup(trackBodySvgGroup, blockSvgGroup);
        }
    }

    var maxLevel = this.updateMaxLevel(trackBodySvgGroup);
    //console.log("DEBUG - maxLevel:" + maxLevel);

    trackBodySvgGroup.__abrowse__height = (this.levelHeight + this.spacingBetweenLevel) * maxLevel;
    trackHeaderSvgGroup.__abrowse__height = this.headerFontSize;
    trackSvgGroup.__abrowse__height = trackBodySvgGroup.__abrowse__height + this.headerFontSize + this.trackBodyMarginTop;

    if (trackSvgGroupComplexus.initialCreated) {

        trackSvgGroup.__abrowse__matrix_f = top;

        trackHeaderSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, 0, top));
        trackHeaderSvgGroup.__abrowse__matrix_f = top;

        var a = this.genomeBrowser.svgDisplayScale;
        var b = 0;
        var c = 0;
        var d = 1;
        var e = this.genomeBrowser.abrowseMatrixE;
        /*
        if (trackBodySvgGroup.__abrowse__matrix_e) {
            e = trackBodySvgGroup.__abrowse__matrix_e;
        } else {
            trackBodySvgGroup.__abrowse__matrix_e = e;
        }
        */

        var f = top + this.trackBodyMarginTop;
        trackBodySvgGroup.__abrowse__matrix_f = f;

        trackBodySvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(a, b, c, d, e, f));
    }
    return trackSvgGroupComplexus;
};

ABrowse.view.GeneModelLikeView.prototype.slide = function (trackSvgGroup, slideStepInPixels, top) {

    trackSvgGroup.__abrowse__matrix_f = top;

    var trackHeaderSvgGroup = trackSvgGroup.childNodes[0];
    var trackBodySvgGroup = trackSvgGroup.childNodes[1];
    var maxLevel = this.updateMaxLevel(trackBodySvgGroup);

    trackBodySvgGroup.__abrowse__height = (this.levelHeight + this.spacingBetweenLevel) * maxLevel;
    trackSvgGroup.__abrowse__height = trackBodySvgGroup.__abrowse__height + this.headerFontSize + this.trackBodyMarginTop;

    trackHeaderSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, 0, top));
    trackHeaderSvgGroup.__abrowse__matrix_f = top;

    var a = this.genomeBrowser.svgDisplayScale;
    var b = 0;
    var c = 0;
    var d = 1;
    /*
    var e = trackBodySvgGroup.__abrowse__matrix_e - slideStepInPixels;
    trackBodySvgGroup.__abrowse__matrix_e = e;
    */
    var e = this.genomeBrowser.abrowseMatrixE;
    var f = top + this.trackBodyMarginTop;
    trackBodySvgGroup.__abrowse__matrix_f = f;

    trackBodySvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(a, b, c, d, e, f));
};
