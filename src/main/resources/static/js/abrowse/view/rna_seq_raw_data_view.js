ABrowse.view.RNASeqRawDataView = function (genomeBrowser) {
    ABrowse.view.GeneModelLikeView.call(this, genomeBrowser);
    //this.trackHeight = 100;
    this.fontHeight = 10;
    this.halfReadHeight = 2;
    this.halfEntryHeight = 5;
    this.levelHeight = 10;
    this.spacingBetweenLevel = 2;
};

ABrowse.view.RNASeqRawDataView.prototype = new ABrowse.view.GeneModelLikeView();


ABrowse.view.RNASeqRawDataView.prototype.drawEntry = function (entry, level, trackName) {

    var ORIGINAL_POINT_X = this.genomeBrowser.originalPointX;
    var left = entry.left;
    var start = left.POS - ORIGINAL_POINT_X;
    var end = left.POS + left.TLEN;

    var entryAnchorId = ABrowse.view.createEntryAnchorId(trackName, left.QNAME);
    var entryAnchor = document.getElementById(entryAnchorId);
    if (!entryAnchor) {
        entryAnchor = document.createElementNS(ABrowse.SVG_NS, "a");
        entryAnchor.id = entryAnchorId;
        entryAnchor.addEventListener("click", ABrowse.view.GeneModelLikeView.entryClickEventHandler, false);
        entryAnchor.__abrowse__level = level;
        entryAnchor.__abrowse__start = start;
        entryAnchor.__abrowse__end = end;
        entryAnchor.setAttribute("style", "cursor:pointer;");
        /* this line works for FF17 IE9 */
        entryAnchor.setAttributeNS(ABrowse.XLINK_NS, "style", "cursor:pointer;");
        /* this line and prev line work together for Chrome23 */
    } else {
        return null;
    }

    var pointsArray;
    var polyline;

    var re = /(\d+)([MIDNSHP=X])/g;
    var M = null;
    while (M = re.exec(left.CIGAR)) {
        var length = parseInt(M[1]);
        var op = M[2];

        var y1 = (level - 1) * (this.levelHeight + this.spacingBetweenLevel);   // level starts from 1
        switch (op) {
            case "M":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));

                // 这里的 ["+"] 为临时实现，目前没有对read的strand进行判断，以下同
                //polyline.setAttribute("fill", this.colorSchema["+"]["read"]);
                polyline.setAttribute("style", "fill:darkred;stroke:darkred;stroke-width:1");

                entryAnchor.appendChild(polyline);

                start += length; //M consumes reference
                break;
            case "I":
                /*
                var bases = 2 / this.genomeBrowser.svgDisplayScale;

                var y1 = (level - 1) * this.levelHeight;
                var y2 = y1 + this.spacingBetweenLevel + 1;
                var x1 = start - bases;
                var x2 = start;
                var x3 = start + bases;
                pointsArray = [];
                pointsArray.push([x1,y1].join(","));
                pointsArray.push([x2,y2].join(","));
                pointsArray.push([x3,y1].join(","));
                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));
                polyline.setAttribute("style", "fill:white;stroke:red;stroke-width:2");
                entryAnchor.appendChild(polyline);
                //I does not consumes reference
                */
                break;
            case "D":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));
                polyline.setAttribute("style", "fill:darkgray;stroke:darkgray;stroke-width:1");
                entryAnchor.appendChild(polyline);

                start += length; //D consumes reference
                break;
            case "N":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));
                polyline.setAttribute("style", "fill:white;stroke:darkgray;stroke-width:1");
                entryAnchor.appendChild(polyline);

                start += length; //N consumes reference
                break;
            case "S":
                //S does not consumes referenc
                break;
            case "H":
                //H does not consumes referenc
                break;
            case "P":
                //P does not consumes referenc
                break;
            case "=":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));

                // 这里的 ["+"] 为临时实现，目前没有对read的strand进行判断，以下同
                //polyline.setAttribute("fill", this.colorSchema["+"]["read"]);
                polyline.setAttribute("style", "fill:darkred;stroke:darkred;stroke-width:1");

                entryAnchor.appendChild(polyline);

                start += length; //= consumes reference
                break;
            case "X":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));

                polyline.setAttribute("style", "fill:white;stroke:darkgray;stroke-width:1");

                entryAnchor.appendChild(polyline);

                start += length; //M consumes reference
                break;
        }
    }

    var right = entry.right;
    var rightStart = right.POS - ORIGINAL_POINT_X;
    var y1 = (level - 1) * (this.levelHeight + this.spacingBetweenLevel) + this.halfReadHeight;
    var y2 = y1 + this.halfEntryHeight;
    var x1 = start;
    var x2 = rightStart;
    pointsArray = [];
    pointsArray.push([x1,y1].join(","));
    pointsArray.push([x2,y2].join(","));
    polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
    polyline.setAttribute("points", pointsArray.join(" "));
    polyline.setAttribute("style", "fill:darkgray;stroke:darkgray;stroke-width:1");
    entryAnchor.appendChild(polyline);

    start = rightStart;

    M = null;
    while (M = re.exec(right.CIGAR)) {
        var length = parseInt(M[1]);
        var op = M[2];

        var y1 = (level - 1) * (this.levelHeight + this.spacingBetweenLevel) + this.halfEntryHeight;   // level starts from 1
        switch (op) {
            case "M":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));

                // 这里的 ["+"] 为临时实现，目前没有对read的strand进行判断，以下同
                // polyline.setAttribute("fill", this.colorSchema["+"]["read"]);
                polyline.setAttribute("style", "fill:darkblue;stroke:darkblue;stroke-width:1");

                entryAnchor.appendChild(polyline);

                start += length; //M consumes reference
                break;
            case "I":
                /*
                var bases = 2 / this.genomeBrowser.svgDisplayScale;

                var y1 = (level - 1) * this.levelHeight;
                var y2 = y1 + this.spacingBetweenLevel + 1;
                var x1 = start - bases;
                var x2 = start;
                var x3 = start + bases;
                pointsArray = [];
                pointsArray.push([x1,y1].join(","));
                pointsArray.push([x2,y2].join(","));
                pointsArray.push([x3,y1].join(","));
                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));
                polyline.setAttribute("style", "fill:white;stroke:red;stroke-width:2");
                entryAnchor.appendChild(polyline);
                //I does not consumes reference
                */
                break;
            case "D":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));
                polyline.setAttribute("style", "fill:darkgray;stroke:darkgray;stroke-width:1");
                entryAnchor.appendChild(polyline);

                start += length; //D consumes reference
                break;
            case "N":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));
                polyline.setAttribute("style", "fill:white;stroke:darkgray;stroke-width:1");
                entryAnchor.appendChild(polyline);

                start += length; //N consumes reference
                break;
            case "S":
                //S does not consumes referenc
                break;
            case "H":
                //H does not consumes referenc
                break;
            case "P":
                //P does not consumes referenc
                break;
            case "=":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));

                // 这里的 ["+"] 为临时实现，目前没有对read的strand进行判断，以下同
                // polyline.setAttribute("fill", this.colorSchema["+"]["read"]);
                polyline.setAttribute("style", "fill:darkblue;stroke:darkblue;stroke-width:1");

                entryAnchor.appendChild(polyline);

                start += length; //= consumes reference
                break;
            case "X":
                var y2 = y1 + this.halfReadHeight * 2;
                var x1 = start;
                var x2 = start + length;
                pointsArray = [];
                pointsArray.push([x1, y1].join(","));
                pointsArray.push([x2, y1].join(","));
                pointsArray.push([x2, y2].join(","));
                pointsArray.push([x1, y2].join(","));
                pointsArray.push([x1, y1].join(","));

                polyline = document.createElementNS(ABrowse.SVG_NS, "polyline");
                polyline.setAttribute("points", pointsArray.join(" "));

                polyline.setAttribute("style", "fill:white;stroke:darkgray;stroke-width:1");

                entryAnchor.appendChild(polyline);

                start += length; //M consumes reference
                break;
        }
    }

    //entryAnchor.__abrowse__max_level = level;
    return entryAnchor;

};

ABrowse.view.RNASeqRawDataView.prototype.drawBlock = function (blockResponse, trackName) {

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
    /* 排序很重要，关系到read pair在图中如何排列 */
    entries.sort(function (a, b) {
        return parseFloat(a.left.POS) - parseFloat(b.left.POS);
    });

    for (var idx = 0; idx < entries.length; ++idx) {
        var level = 1;
        var pair = entries[idx];
        var start = pair.left.POS;

        for (var innerIdx = 0; innerIdx < idx; ++innerIdx) {
            var anotherPair = entries[innerIdx];
            var anotherEnd = anotherPair.left.POS + anotherPair.left.TLEN;
            //if (start < anotherEnd) {
            /* +100 是为了让靠的比较近的reads在显示的时候分的更开一些 */
            if (start < anotherEnd + 100) {
                level = level + 1;
            }
        }

        var pairSvgGroup = this.drawEntry(pair, level, trackName);
        if (null != pairSvgGroup) {
            blockSvgGroup.appendChild(pairSvgGroup);
            if (blockSvgGroup.__abrowse__max_level < pairSvgGroup.__abrowse__level) {
                blockSvgGroup.__abrowse__max_level = pairSvgGroup.__abrowse__level;
            }
        }
    }

    return blockSvgGroup;
};