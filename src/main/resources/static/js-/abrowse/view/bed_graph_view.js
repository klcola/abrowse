ABrowse.view.BedGraphView = function (genomeBrowser) {
    ABrowse.view.View.call(this, genomeBrowser);
    this.trackBodyHeight = 100;
    this.fontHeight = 10;
};

ABrowse.view.BedGraphView.prototype = new ABrowse.view.View();

ABrowse.view.BedGraphView.prototype.drawVerticalScaleMark = function (blockSvgGroup, maxValueInBlock) {
    var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;
    var markNum = 5;
    var unitScore = (maxValueInBlock / markNum).toFixed(2);

    var scaleMarkSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
    var scalePixels = (this.trackBodyHeight / markNum).toFixed(0);
    var svgDisplayScale = this.genomeBrowser.svgDisplayScale;
    for (var idx = 0; idx < markNum; ++idx) {
        var markLine = document.createElementNS(ABrowse.SVG_NS, "polyline");
        var y = idx * scalePixels;
        var pointsArray = [];
        pointsArray.push([blockSvgGroup.__abrowse__start - ORIGINAL_POINT_X, y].join(","));
        pointsArray.push([blockSvgGroup.__abrowse__end - ORIGINAL_POINT_X, y].join(","));
        pointsArray.push([blockSvgGroup.__abrowse__end - ORIGINAL_POINT_X, y + 1].join(","));
        pointsArray.push([blockSvgGroup.__abrowse__start - ORIGINAL_POINT_X, y + 1].join(","));
        markLine.setAttribute("points", pointsArray.join(" "));
        markLine.setAttribute("fill", "rgb(230,230,230)");
        scaleMarkSvgGroup.appendChild(markLine);

        var text = document.createElementNS(ABrowse.SVG_NS, "text");
        text.textContent = (unitScore * (markNum - idx)).toFixed(2) === 'NaN' ? '' : (unitScore * (markNum - idx)).toFixed(2);
        var fontSize = (this.fontHeight / svgDisplayScale).toFixed(0);
        var fontY = ( (y + this.fontHeight + 2) / svgDisplayScale).toFixed(0);
        text.setAttribute("x", (blockSvgGroup.__abrowse__start - ORIGINAL_POINT_X).toString());
        text.setAttribute("y", fontY);
        text.setAttribute("transform", ["scale(1,", svgDisplayScale, ")"].join(""));
        text.setAttribute("font-size", fontSize + "px");
        text.setAttribute("font-family", "Calibri, Arial, Helvetica, sans-serif");
        text.setAttribute("font-weight", "bold");
        text.setAttribute("fill", "black");
        scaleMarkSvgGroup.appendChild(text);
    }

    return scaleMarkSvgGroup;
};

ABrowse.view.BedGraphView.prototype.drawBlock = function (blockResponse, trackName, trackSvgGroup) {

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

    /*
    entries.sort(function (a, b) {
        return parseFloat(a.value) - parseFloat(b.value);
    });
    */
    entries.sort(function (a, b) {
        return parseFloat(a.start) - parseFloat(b.start);
    });

    blockSvgGroup.__abrowse__entries = entries;

    var valueMean = 0;
    entries.forEach(function (item, index, array) { valueMean += item.value });

    valueMean = valueMean / entries.length;
    if (valueMean < 10) {
        valueMean = 10;
    }

    blockSvgGroup.__abrowse__value_mean_in_block = valueMean;

    // if (valueMean > trackSvgGroup.__abrowse__value_mean) {
    //     trackSvgGroup.__abrowse__value_mean = valueMean;
    // } else {
    //     valueMean = trackSvgGroup.__abrowse__value_mean;
    // }

    var maxVal = valueMean * 3;

    var scaleMarkSvgGroup = this.drawVerticalScaleMark(blockSvgGroup, maxVal);
    blockSvgGroup.appendChild(scaleMarkSvgGroup);

    var pointsArray = [];

    var x2, y2;
    for (var idx = 0; idx < entries.length; ++idx) {
        var entry = entries[idx];
        var start = entry.start;
        var length = entry.end - entry.start;
        var value = entry.value;


        var x1 = start - ORIGINAL_POINT_X;

        pointsArray.push([x1, this.trackBodyHeight ].join(" "));

        var y1 = (this.trackBodyHeight ) - ((value / maxVal ) * this.trackBodyHeight).toFixed(0);

        pointsArray.push([x1, y1].join(" "));

        if (length > 1) {
            x2 = x1 + length - 1;
            y2 = y1;
            pointsArray.push([x2, y2].join(" "));
        }
    }

    pointsArray.push([x2, this.trackBodyHeight ].join(" "));
    pointsArray.push([x1, this.trackBodyHeight ].join(" "));

    /*
    var path = document.createElementNS(ABrowse.SVG_NS, "path");
    path.setAttribute("d", "M" + pointsArray.join(" "));
    path.setAttribute("stroke-linejoin", "round");
    path.setAttribute("stroke-width", "1");
    path.setAttribute("stroke", "darkred");
    path.setAttribute("fill", "darkred");
    blockSvgGroup.appendChild(path);
    */
    var polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
    polyline.setAttribute("points", pointsArray.join(" "));
    polyline.setAttribute("fill", "darkred");
    blockSvgGroup.appendChild(polyline);
    pointsArray = [];

    /*
    var blockAnchor = document.createElementNS(ABrowse.SVG_NS, "a");
    blockAnchor.setAttributeNS(ABrowse.XLINK_NS, "xlink:href", "/data?trackName=" + trackName + "&amp;c=chr1"
        + "&amp;s=" + blockResponse.start + "&amp;e=" + blockResponse.end);

    blockAnchor.appendChild(path);
    blockSvgGroup.appendChild(blockAnchor);
    */

    return blockSvgGroup;
};

ABrowse.view.BedGraphView.prototype.render = function (trackResponse, top) {

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

ABrowse.view.BedGraphView.prototype.updateMaxValue = function (trackSvgGroup) {
    var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;

    var valueMean = trackSvgGroup.__abrowse__value_mean;
    var trackBodySvgGroup = trackSvgGroup.childNodes[1];

    for (var i = 0; i < trackBodySvgGroup.childNodes.length; ++i) {
        var blockSvgGroup = trackBodySvgGroup.childNodes[i];
        if (this.genomeBrowser.viewableLoc.isIncludingPoint(blockSvgGroup.__abrowse__start)
            || this.genomeBrowser.viewableLoc.isIncludingPoint(blockSvgGroup.__abrowse__end)) {
            if (blockSvgGroup.__abrowse__value_mean_in_block < valueMean) {
                blockSvgGroup.__abrowse__value_mean_in_block = valueMean;

                var entries = blockSvgGroup.__abrowse__entries;
                blockSvgGroup.innerHTML = "";

                var maxVal = valueMean * 3;

                var scaleMarkSvgGroup = this.drawVerticalScaleMark(blockSvgGroup, maxVal);
                blockSvgGroup.appendChild(scaleMarkSvgGroup);

                var pointsArray = [];

                var x2, y2;
                for (var idx = 0; idx < entries.length; ++idx) {
                    var entry = entries[idx];
                    var start = entry.start;
                    var length = entry.end - entry.start;
                    var value = entry.value;


                    var x1 = start - ORIGINAL_POINT_X;
                    pointsArray.push([x1, this.trackBodyHeight - 1].join(" "));
                    var y1 = (this.trackBodyHeight - 1) - ((value / maxVal) * this.trackBodyHeight).toFixed(0);

                    pointsArray.push([x1, y1].join(" "));

                    if (length > 1) {
                        x2 = x1 + length - 1;
                        y2 = y1;
                        pointsArray.push([x2, y2].join(" "));
                    }
                }

                pointsArray.push([x2, this.trackBodyHeight - 1].join(" "));
                pointsArray.push([x1, this.trackBodyHeight - 1].join(" "));

                /*
                var path = document.createElementNS(ABrowse.SVG_NS, "path");
                path.setAttribute("d", "M" + pointsArray.join(" "));
                path.setAttribute("stroke-linejoin", "round");
                path.setAttribute("stroke-width", "1");
                path.setAttribute("stroke", "darkred");
                path.setAttribute("fill", "darkred");
                blockSvgGroup.appendChild(path);
                */
                var polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));
                polyline.setAttribute("fill", "darkred");
                blockSvgGroup.appendChild(polyline);

                pointsArray = [];

            }
        }
    }
};

ABrowse.view.BedGraphView.prototype.slide = function (trackSvgGroup, slideStepInPixels, top) {

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
