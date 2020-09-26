ABrowse.view.HICView = function (genomeBrowser) {
    ABrowse.view.View.call(this, genomeBrowser);
    this.trackBodyHeight = 100;
    this.fontHeight = 10;
};

ABrowse.view.HICView.prototype = new ABrowse.view.View();

ABrowse.view.HICView.prototype.drawBlock = function (blockResponse, trackName, trackSvgGroup){
    var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;

    var blockSvgGroupId = ABrowse.view.createBlockSvgGroupId(trackName, blockResponse.start, blockResponse.end);
    var blockSvgGroup = document.getElementById(blockSvgGroupId);
    if (!blockSvgGroup) {
        blockSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
        blockSvgGroup.id = blockSvgGroupId;
        blockSvgGroup.__abrowse__start = blockResponse.start;
        blockSvgGroup.__abrowse__end = blockResponse.end;
    } else {
        return null;
    }
    var entries = blockResponse.entryList;

    entries.sort(function (a, b) {
        return parseFloat(a.start) - parseFloat(b.start);
    });

    blockSvgGroup.__abrowse__entries = entries;
    var x = 0;
    var y = 0;
    for (var idx = 0; idx < entries.length; ++idx) {
        var entry = entries[idx];
        x = entry.start;
        // var binBlock = document.createElementNS(ABrowse.SVG_NS, "g");
        // binBlock.id = blockResponse.start + "" + new Date().getTime();
        for (var binInd = 0;binInd<entry.binList.length;binInd++){
            var bin = entry.binList[binInd];
            if (entry.chr != bin.targetChr){
                continue;
            }
            if (bin.targetStart > blockResponse.end){
                continue;
            }
            if (bin.targetEnd < blockResponse.start){
                continue;
            }
            // x = x + 10000 * binInd;
            // y = (bin.targetStart - entry.start);
            y = (bin.targetStart - entry.start)/1000;
            var rect = document.createElementNS(ABrowse.SVG_NS, "rect");
            rect.setAttribute("x", x + "");
            rect.setAttribute("y", y + "");
            rect.setAttribute("width", "20000");
            rect.setAttribute("height", "20");
            rect.setAttribute("fill", "#ff0000");
            rect.setAttribute("opacity", bin.value/15 + "");
            // rect.setAttribute("transform","rotate(" + x + "," + y + " 45)");
            blockSvgGroup.appendChild(rect);

            // var polygon =  document.createElementNS(ABrowse.SVG_NS, "polygon");
            // var points = [];
            // points.push(x + "," + y);
            // points.push((x + 10000) + "," + (y - 10000));
            // points.push((x + 20000) + "," + y);
            // points.push((x + 10000) + "," + (y + 10000));
            // polygon.setAttribute("points",points.join(" "));
            // polygon.setAttribute("fill", "#ff0000");
            // polygon.setAttribute("opacity", bin.value/15 + "");
            // binBlock.appendChild(polygon);
        }
        // binBlock.setAttribute("transform","rotate(" + (x + 20000 * idx) + "," + 0 + " 45)");
        // blockSvgGroup.appendChild(binBlock);
    }
    return blockSvgGroup;
}

ABrowse.view.HICView.prototype.render = function (trackResponse, top) {
    var trackSvgGroupComplexus = this.initializeRender(trackResponse);
    var trackSvgGroup = trackSvgGroupComplexus.trackSvgGroup;
    trackSvgGroup.__abrowse__trackView = this;
    trackSvgGroup.__abrowse__value_mean = 0;

    var trackHeaderSvgGroup = trackSvgGroup.childNodes[0];
    var trackBodySvgGroup = trackSvgGroup.childNodes[1];

    var blockResponses = trackResponse.blockResponses;
    for (var j = 0; j < blockResponses.length; ++j) {
        var blockSvgGroup = this.drawBlock(blockResponses[j], trackResponse.trackName, trackSvgGroup);
        if (null != blockSvgGroup) {
            this.insertBlockSvgGroup(trackBodySvgGroup, blockSvgGroup);
        }
    }
    trackBodySvgGroup.__abrowse__height = this.trackBodyHeight;
    trackHeaderSvgGroup.__abrowse__height = this.headerFontSize;
    trackSvgGroup.__abrowse__height = trackBodySvgGroup.__abrowse__height + trackHeaderSvgGroup.__abrowse__height
        + this.trackBodyMarginTop;

    if (trackSvgGroupComplexus.initialCreated) {

        trackSvgGroup.__abrowse__matrix_f = top;

        trackHeaderSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, 0, top));
        trackHeaderSvgGroup.__abrowse__matrix_f = top;

        var a = this.genomeBrowser.svgDisplayScale;
        var b = 0;
        var c = 0;
        var d = 1;
        //var d = this.trackBodyHeight / trackSvgGroup.__abrowse__max_value;
        trackSvgGroup.__abrowse__matrix_d = d;
        var e = this.genomeBrowser.abrowseMatrixE;

        var f = top + this.trackBodyMarginTop;
        trackBodySvgGroup.__abrowse__matrix_f = f;

        trackBodySvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(a, b, c, d, e, f));
    }

    return trackSvgGroupComplexus;

};

ABrowse.view.HICView.prototype.slide = function (trackSvgGroup, slideStepInPixels, top) {

    trackSvgGroup.__abrowse__matrix_f = top;

    //this.updateMaxValue(trackSvgGroup);
    var trackHeaderSvgGroup = trackSvgGroup.childNodes[0];
    var trackBodySvgGroup = trackSvgGroup.childNodes[1];

    trackHeaderSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, 0, top));
    trackHeaderSvgGroup.__abrowse__matrix_f = top;

    var a = this.genomeBrowser.svgDisplayScale;
    var b = 0;
    var c = 0;
    //var d = 1;
    var d = trackSvgGroup.__abrowse__matrix_d;

    var e = this.genomeBrowser.abrowseMatrixE;
    var f = top + this.trackBodyMarginTop;
    trackBodySvgGroup.__abrowse__matrix_f = f;

    trackBodySvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(a, b, c, d, e, f));

};
