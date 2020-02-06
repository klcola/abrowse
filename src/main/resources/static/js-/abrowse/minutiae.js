
ABrowse.getIEMouseXY = function(event) {

    var mouseX = event.clientX + document.body.scrollLeft;
    var mouseY = event.clientY + document.body.scrollTop;

    return {x:mouseX, y:mouseY};
};

ABrowse.getGeckoMouseXY = function(event) {
    return {x:event.pageX, y:event.pageY};
};

ABrowse.getMouseXY = undefined;

ABrowse.isIEMouseLeftButton = function(event) {
    return event.button == 0;
};

ABrowse.isIEMouseRightButton = function(event) {
    return event.button == 2;
};

ABrowse.isMouseLeftButton = undefined;

ABrowse.isGeckoMouseLeftButton = function(event) {
    return event.button == 0;
};

ABrowse.isGeckoMouseRightButton = function(event) {
    return event.button == 2;
};

ABrowse.isMouseRightButton = undefined;

ABrowse.parsePositionStr = function(positionStr) {

    var searchStr = positionStr.replace(/,/g, '');
    var fields = searchStr.split(':');
    var chrName = fields[0];
    var positionStr = fields[1];

    var sign = 1;
    if ('-' === positionStr.charAt(0)) {
        sign = -1;
        positionStr = positionStr.substr(1);
    }

    var startEndMode = true;
    var positions = positionStr.split('-');
    if (positions.length != 2) {
        positions = positionStr.split('+');
        //return null;
        if (positions.length == 2) {
            startEndMode = false;
        } else {
            window.alert("Search string for simple search must be in the format (1)chr:start-end, like 'chr2:175000-196000'. (2)chr:start+length, like 'chr2:175000+21000'. (3)gene ID, like 'AT1G28060.1'.");
            return null;
        }
    }

    var start = parseInt(positions[0]) * sign;
    var end;
    if (startEndMode) {
        end = parseInt(positions[1]);
    } else {
        var length = parseInt(positions[1]);
        end = start + length;
    }

    if (end <= start) {
        window.alert("chromosome end coordinate must be bigger than start coordinate!");
        return null;
    }

    return new ABrowse.ChromosomeLocation(chrName, start, end);

};
