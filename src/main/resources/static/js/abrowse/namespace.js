if (typeof ABrowse == "undefined") {
    var ABrowse = {};
}

ABrowse.namespace = function() {
    var a = arguments, o = null, i, j, d;
    for (i = 0; i < a.length; i = i + 1) {
        d = a[i].split(".");
        o = window;
        for (j = 0; j < d.length; j = j + 1) {
            o[d[j]] = o[d[j]] || {};
            o = o[d[j]];
        }
    }
    return o;
};

ABrowse.namespace('ABrowse');
ABrowse.namespace("ABrowse.browse");
ABrowse.namespace("ABrowse.option");
ABrowse.namespace("ABrowse.view");