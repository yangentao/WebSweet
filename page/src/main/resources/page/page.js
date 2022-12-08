
function invokeElementAttr(elem, attr){
    if(elem.hasAttribute(attr)) {
        const fun = new Function(elem.getAttribute(attr));
        fun.call(elem);
    }
}