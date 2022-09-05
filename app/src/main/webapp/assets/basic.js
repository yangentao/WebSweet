
function invokeElementAttr(elem, attr){
    if(elem.hasAttribute(attr)) {
        var fun = new Function(elem.getAttribute(attr));
        fun.call(elem);
    }
}