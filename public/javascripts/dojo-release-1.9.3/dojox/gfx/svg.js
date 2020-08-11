//>>built
define("dojox/gfx/svg","dojo/_base/lang dojo/_base/sniff dojo/_base/window dojo/dom dojo/_base/declare dojo/_base/array dojo/dom-geometry dojo/dom-attr dojo/_base/Color ./_base ./shape ./path".split(" "),function(l,n,p,q,k,u,x,t,y,h,g,s){function m(a,b){return p.doc.createElementNS?p.doc.createElementNS(a,b):p.doc.createElement(b)}function v(a){return c.useSvgWeb?p.doc.createTextNode(a,!0):p.doc.createTextNode(a)}var c=h.svg={};c.useSvgWeb="undefined"!=typeof window.svgweb;var z=navigator.userAgent,
A=n("ios"),w=n("android"),B=n("chrome")||w&&4<=w?"auto":"optimizeLegibility";c.xmlns={xlink:"http://www.w3.org/1999/xlink",svg:"http://www.w3.org/2000/svg"};c.getRef=function(a){return!a||"none"==a?null:a.match(/^url\(#.+\)$/)?q.byId(a.slice(5,-1)):a.match(/^#dojoUnique\d+$/)?q.byId(a.slice(1)):null};c.dasharray={solid:"none",shortdash:[4,1],shortdot:[1,1],shortdashdot:[4,1,1,1],shortdashdotdot:[4,1,1,1,1,1],dot:[1,3],dash:[4,3],longdash:[8,3],dashdot:[4,3,1,3],longdashdot:[8,3,1,3],longdashdotdot:[8,
3,1,3,1,3]};var C=0;c.Shape=k("dojox.gfx.svg.Shape",g.Shape,{destroy:function(){if(this.fillStyle&&"type"in this.fillStyle){var a=this.rawNode.getAttribute("fill");(a=c.getRef(a))&&a.parentNode.removeChild(a)}if(this.clip&&(a=this.rawNode.getAttribute("clip-path")))(a=q.byId(a.match(/gfx_clip[\d]+/)[0]))&&a.parentNode.removeChild(a);g.Shape.prototype.destroy.apply(this,arguments)},setFill:function(a){if(!a)return this.fillStyle=null,this.rawNode.setAttribute("fill","none"),this.rawNode.setAttribute("fill-opacity",
0),this;var b,d=function(a){this.setAttribute(a,b[a].toFixed(8))};if("object"==typeof a&&"type"in a){switch(a.type){case "linear":b=h.makeParameters(h.defaultLinearGradient,a);a=this._setFillObject(b,"linearGradient");u.forEach(["x1","y1","x2","y2"],d,a);break;case "radial":b=h.makeParameters(h.defaultRadialGradient,a);a=this._setFillObject(b,"radialGradient");u.forEach(["cx","cy","r"],d,a);break;case "pattern":b=h.makeParameters(h.defaultPattern,a),a=this._setFillObject(b,"pattern"),u.forEach(["x",
"y","width","height"],d,a)}this.fillStyle=b;return this}this.fillStyle=b=h.normalizeColor(a);this.rawNode.setAttribute("fill",b.toCss());this.rawNode.setAttribute("fill-opacity",b.a);this.rawNode.setAttribute("fill-rule","evenodd");return this},setStroke:function(a){var b=this.rawNode;if(!a)return this.strokeStyle=null,b.setAttribute("stroke","none"),b.setAttribute("stroke-opacity",0),this;if("string"==typeof a||l.isArray(a)||a instanceof y)a={color:a};a=this.strokeStyle=h.makeParameters(h.defaultStroke,
a);a.color=h.normalizeColor(a.color);if(a){b.setAttribute("stroke",a.color.toCss());b.setAttribute("stroke-opacity",a.color.a);b.setAttribute("stroke-width",a.width);b.setAttribute("stroke-linecap",a.cap);"number"==typeof a.join?(b.setAttribute("stroke-linejoin","miter"),b.setAttribute("stroke-miterlimit",a.join)):b.setAttribute("stroke-linejoin",a.join);var d=a.style.toLowerCase();d in c.dasharray&&(d=c.dasharray[d]);if(d instanceof Array){var d=l._toArray(d),e;for(e=0;e<d.length;++e)d[e]*=a.width;
if("butt"!=a.cap){for(e=0;e<d.length;e+=2)d[e]-=a.width,1>d[e]&&(d[e]=1);for(e=1;e<d.length;e+=2)d[e]+=a.width}d=d.join(",")}b.setAttribute("stroke-dasharray",d);b.setAttribute("dojoGfxStrokeStyle",a.style)}return this},_getParentSurface:function(){for(var a=this.parent;a&&!(a instanceof h.Surface);a=a.parent);return a},_setFillObject:function(a,b){var d=c.xmlns.svg;this.fillStyle=a;var e=this._getParentSurface().defNode,f=this.rawNode.getAttribute("fill");if(f=c.getRef(f))if(f.tagName.toLowerCase()!=
b.toLowerCase()){var g=f.id;f.parentNode.removeChild(f);f=m(d,b);f.setAttribute("id",g);e.appendChild(f)}else for(;f.childNodes.length;)f.removeChild(f.lastChild);else f=m(d,b),f.setAttribute("id",h._base._getUniqueId()),e.appendChild(f);if("pattern"==b)f.setAttribute("patternUnits","userSpaceOnUse"),d=m(d,"image"),d.setAttribute("x",0),d.setAttribute("y",0),d.setAttribute("width",a.width.toFixed(8)),d.setAttribute("height",a.height.toFixed(8)),d.setAttributeNS?d.setAttributeNS(c.xmlns.xlink,"xlink:href",
a.src):d.setAttribute("xlink:href",a.src),f.appendChild(d);else{f.setAttribute("gradientUnits","userSpaceOnUse");for(e=0;e<a.colors.length;++e){var g=a.colors[e],k=m(d,"stop"),l=g.color=h.normalizeColor(g.color);k.setAttribute("offset",g.offset.toFixed(8));k.setAttribute("stop-color",l.toCss());k.setAttribute("stop-opacity",l.a);f.appendChild(k)}}this.rawNode.setAttribute("fill","url(#"+f.getAttribute("id")+")");this.rawNode.removeAttribute("fill-opacity");this.rawNode.setAttribute("fill-rule","evenodd");
return f},_applyTransform:function(){if(this.matrix){var a=this.matrix;this.rawNode.setAttribute("transform","matrix("+a.xx.toFixed(8)+","+a.yx.toFixed(8)+","+a.xy.toFixed(8)+","+a.yy.toFixed(8)+","+a.dx.toFixed(8)+","+a.dy.toFixed(8)+")")}else this.rawNode.removeAttribute("transform");return this},setRawNode:function(a){a=this.rawNode=a;"image"!=this.shape.type&&a.setAttribute("fill","none");a.setAttribute("fill-opacity",0);a.setAttribute("stroke","none");a.setAttribute("stroke-opacity",0);a.setAttribute("stroke-width",
1);a.setAttribute("stroke-linecap","butt");a.setAttribute("stroke-linejoin","miter");a.setAttribute("stroke-miterlimit",4);a.__gfxObject__=this},setShape:function(a){this.shape=h.makeParameters(this.shape,a);for(var b in this.shape)"type"!=b&&this.rawNode.setAttribute(b,this.shape[b]);this.bbox=null;return this},_moveToFront:function(){this.rawNode.parentNode.appendChild(this.rawNode);return this},_moveToBack:function(){this.rawNode.parentNode.insertBefore(this.rawNode,this.rawNode.parentNode.firstChild);
return this},setClip:function(a){this.inherited(arguments);var b=a?"width"in a?"rect":"cx"in a?"ellipse":"points"in a?"polyline":"d"in a?"path":null:null;if(a&&!b)return this;"polyline"===b&&(a=l.clone(a),a.points=a.points.join(","));var d,e=t.get(this.rawNode,"clip-path");e&&(d=q.byId(e.match(/gfx_clip[\d]+/)[0]))&&d.removeChild(d.childNodes[0]);a?(d?(b=m(c.xmlns.svg,b),d.appendChild(b)):(e="gfx_clip"+ ++C,this.rawNode.setAttribute("clip-path","url(#"+e+")"),d=m(c.xmlns.svg,"clipPath"),b=m(c.xmlns.svg,
b),d.appendChild(b),this.rawNode.parentNode.appendChild(d),t.set(d,"id",e)),t.set(b,a)):(this.rawNode.removeAttribute("clip-path"),d&&d.parentNode.removeChild(d));return this},_removeClipNode:function(){var a,b=t.get(this.rawNode,"clip-path");b&&(a=q.byId(b.match(/gfx_clip[\d]+/)[0]))&&a.parentNode.removeChild(a);return a}});c.Group=k("dojox.gfx.svg.Group",c.Shape,{constructor:function(){g.Container._init.call(this)},setRawNode:function(a){this.rawNode=a;this.rawNode.__gfxObject__=this},destroy:function(){this.clear(!0);
c.Shape.prototype.destroy.apply(this,arguments)}});c.Group.nodeType="g";c.Rect=k("dojox.gfx.svg.Rect",[c.Shape,g.Rect],{setShape:function(a){this.shape=h.makeParameters(this.shape,a);this.bbox=null;for(var b in this.shape)"type"!=b&&"r"!=b&&this.rawNode.setAttribute(b,this.shape[b]);null!=this.shape.r&&(this.rawNode.setAttribute("ry",this.shape.r),this.rawNode.setAttribute("rx",this.shape.r));return this}});c.Rect.nodeType="rect";c.Ellipse=k("dojox.gfx.svg.Ellipse",[c.Shape,g.Ellipse],{});c.Ellipse.nodeType=
"ellipse";c.Circle=k("dojox.gfx.svg.Circle",[c.Shape,g.Circle],{});c.Circle.nodeType="circle";c.Line=k("dojox.gfx.svg.Line",[c.Shape,g.Line],{});c.Line.nodeType="line";c.Polyline=k("dojox.gfx.svg.Polyline",[c.Shape,g.Polyline],{setShape:function(a,b){a&&a instanceof Array?(this.shape=h.makeParameters(this.shape,{points:a}),b&&this.shape.points.length&&this.shape.points.push(this.shape.points[0])):this.shape=h.makeParameters(this.shape,a);this.bbox=null;this._normalizePoints();for(var d=[],c=this.shape.points,
f=0;f<c.length;++f)d.push(c[f].x.toFixed(8),c[f].y.toFixed(8));this.rawNode.setAttribute("points",d.join(" "));return this}});c.Polyline.nodeType="polyline";c.Image=k("dojox.gfx.svg.Image",[c.Shape,g.Image],{setShape:function(a){this.shape=h.makeParameters(this.shape,a);this.bbox=null;a=this.rawNode;for(var b in this.shape)"type"!=b&&"src"!=b&&a.setAttribute(b,this.shape[b]);a.setAttribute("preserveAspectRatio","none");a.setAttributeNS?a.setAttributeNS(c.xmlns.xlink,"xlink:href",this.shape.src):a.setAttribute("xlink:href",
this.shape.src);a.__gfxObject__=this;return this}});c.Image.nodeType="image";c.Text=k("dojox.gfx.svg.Text",[c.Shape,g.Text],{setShape:function(a){this.shape=h.makeParameters(this.shape,a);this.bbox=null;a=this.rawNode;var b=this.shape;a.setAttribute("x",b.x);a.setAttribute("y",b.y);a.setAttribute("text-anchor",b.align);a.setAttribute("text-decoration",b.decoration);a.setAttribute("rotate",b.rotated?90:0);a.setAttribute("kerning",b.kerning?"auto":0);a.setAttribute("text-rendering",B);a.firstChild?
a.firstChild.nodeValue=b.text:a.appendChild(v(b.text));return this},getTextWidth:function(){var a=this.rawNode,b=a.parentNode,a=a.cloneNode(!0);a.style.visibility="hidden";var d=0,c=a.firstChild.nodeValue;b.appendChild(a);if(""!=c)for(;!d;)d=a.getBBox?parseInt(a.getBBox().width):68;b.removeChild(a);return d},getBoundingBox:function(){var a=null;if(this.getShape().text)try{a=this.rawNode.getBBox()}catch(b){a={x:0,y:0,width:0,height:0}}return a}});c.Text.nodeType="text";c.Path=k("dojox.gfx.svg.Path",
[c.Shape,s.Path],{_updateWithSegment:function(a){this.inherited(arguments);"string"==typeof this.shape.path&&this.rawNode.setAttribute("d",this.shape.path)},setShape:function(a){this.inherited(arguments);this.shape.path?this.rawNode.setAttribute("d",this.shape.path):this.rawNode.removeAttribute("d");return this}});c.Path.nodeType="path";c.TextPath=k("dojox.gfx.svg.TextPath",[c.Shape,s.TextPath],{_updateWithSegment:function(a){this.inherited(arguments);this._setTextPath()},setShape:function(a){this.inherited(arguments);
this._setTextPath();return this},_setTextPath:function(){if("string"==typeof this.shape.path){var a=this.rawNode;if(!a.firstChild){var b=m(c.xmlns.svg,"textPath"),d=v("");b.appendChild(d);a.appendChild(b)}b=(b=a.firstChild.getAttributeNS(c.xmlns.xlink,"href"))&&c.getRef(b);if(!b&&(d=this._getParentSurface())){var d=d.defNode,b=m(c.xmlns.svg,"path"),e=h._base._getUniqueId();b.setAttribute("id",e);d.appendChild(b);a.firstChild.setAttributeNS?a.firstChild.setAttributeNS(c.xmlns.xlink,"xlink:href","#"+
e):a.firstChild.setAttribute("xlink:href","#"+e)}b&&b.setAttribute("d",this.shape.path)}},_setText:function(){var a=this.rawNode;if(!a.firstChild){var b=m(c.xmlns.svg,"textPath"),d=v("");b.appendChild(d);a.appendChild(b)}a=a.firstChild;b=this.text;a.setAttribute("alignment-baseline","middle");switch(b.align){case "middle":a.setAttribute("text-anchor","middle");a.setAttribute("startOffset","50%");break;case "end":a.setAttribute("text-anchor","end");a.setAttribute("startOffset","100%");break;default:a.setAttribute("text-anchor",
"start"),a.setAttribute("startOffset","0%")}a.setAttribute("baseline-shift","0.5ex");a.setAttribute("text-decoration",b.decoration);a.setAttribute("rotate",b.rotated?90:0);a.setAttribute("kerning",b.kerning?"auto":0);a.firstChild.data=b.text}});c.TextPath.nodeType="text";var D=534<function(){var a=/WebKit\/(\d*)/.exec(z);return a?a[1]:0}();c.Surface=k("dojox.gfx.svg.Surface",g.Surface,{constructor:function(){g.Container._init.call(this)},destroy:function(){g.Container.clear.call(this,!0);this.defNode=
null;this.inherited(arguments)},setDimensions:function(a,b){if(!this.rawNode)return this;this.rawNode.setAttribute("width",a);this.rawNode.setAttribute("height",b);D&&(this.rawNode.style.width=a,this.rawNode.style.height=b);return this},getDimensions:function(){return this.rawNode?{width:h.normalizedLength(this.rawNode.getAttribute("width")),height:h.normalizedLength(this.rawNode.getAttribute("height"))}:null}});c.createSurface=function(a,b,d){var e=new c.Surface;e.rawNode=m(c.xmlns.svg,"svg");e.rawNode.setAttribute("overflow",
"hidden");b&&e.rawNode.setAttribute("width",b);d&&e.rawNode.setAttribute("height",d);b=m(c.xmlns.svg,"defs");e.rawNode.appendChild(b);e.defNode=b;e._parent=q.byId(a);e._parent.appendChild(e.rawNode);h._base._fixMsTouchAction(e);return e};n={_setFont:function(){var a=this.fontStyle;this.rawNode.setAttribute("font-style",a.style);this.rawNode.setAttribute("font-variant",a.variant);this.rawNode.setAttribute("font-weight",a.weight);this.rawNode.setAttribute("font-size",a.size);this.rawNode.setAttribute("font-family",
a.family)}};var r=g.Container;k={openBatch:function(){if(!this._batch){var a;a=c.useSvgWeb?p.doc.createDocumentFragment(!0):p.doc.createDocumentFragment();this.fragment=a}++this._batch;return this},closeBatch:function(){this._batch=0<this._batch?--this._batch:0;this.fragment&&!this._batch&&(this.rawNode.appendChild(this.fragment),delete this.fragment);return this},add:function(a){this!=a.getParent()&&(this.fragment?this.fragment.appendChild(a.rawNode):this.rawNode.appendChild(a.rawNode),r.add.apply(this,
arguments),a.setClip(a.clip));return this},remove:function(a,b){this==a.getParent()&&(this.rawNode==a.rawNode.parentNode&&this.rawNode.removeChild(a.rawNode),this.fragment&&this.fragment==a.rawNode.parentNode&&this.fragment.removeChild(a.rawNode),a._removeClipNode(),r.remove.apply(this,arguments));return this},clear:function(){for(var a=this.rawNode;a.lastChild;)a.removeChild(a.lastChild);var b=this.defNode;if(b){for(;b.lastChild;)b.removeChild(b.lastChild);a.appendChild(b)}return r.clear.apply(this,
arguments)},getBoundingBox:r.getBoundingBox,_moveChildToFront:r._moveChildToFront,_moveChildToBack:r._moveChildToBack};s={createObject:function(a,b){if(!this.rawNode)return null;var d=new a,e=m(c.xmlns.svg,a.nodeType);d.setRawNode(e);d.setShape(b);this.add(d);return d}};l.extend(c.Text,n);l.extend(c.TextPath,n);l.extend(c.Group,k);l.extend(c.Group,g.Creator);l.extend(c.Group,s);l.extend(c.Surface,k);l.extend(c.Surface,g.Creator);l.extend(c.Surface,s);c.fixTarget=function(a,b){a.gfxTarget||(a.gfxTarget=
A&&a.target.wholeText?a.target.parentElement.__gfxObject__:a.target.__gfxObject__);return!0};c.useSvgWeb&&(c.createSurface=function(a,b,d){var e=new c.Surface;if(!b||!d){var f=x.position(a);b=b||f.w;d=d||f.h}a=q.byId(a);var f=a.id?a.id+"_svgweb":h._base._getUniqueId(),g=m(c.xmlns.svg,"svg");g.id=f;g.setAttribute("width",b);g.setAttribute("height",d);svgweb.appendChild(g,a);g.addEventListener("SVGLoad",function(){e.rawNode=this;e.isLoaded=!0;var a=m(c.xmlns.svg,"defs");e.rawNode.appendChild(a);e.defNode=
a;if(e.onLoad)e.onLoad(e)},!1);e.isLoaded=!1;return e},c.Surface.extend({destroy:function(){var a=this.rawNode;svgweb.removeChild(a,a.parentNode)}}),n={connect:function(a,b,c){"on"===a.substring(0,2)&&(a=a.substring(2));c=2==arguments.length?b:l.hitch(b,c);this.getEventSource().addEventListener(a,c,!1);return[this,a,c]},disconnect:function(a){this.getEventSource().removeEventListener(a[1],a[2],!1);delete a[0]}},l.extend(c.Shape,n),l.extend(c.Surface,n));return c});
//# sourceMappingURL=svg.js.map