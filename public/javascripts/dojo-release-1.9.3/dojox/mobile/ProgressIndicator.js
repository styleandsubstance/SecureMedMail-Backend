//>>built
define("dojox/mobile/ProgressIndicator","dojo/_base/config dojo/_base/declare dojo/_base/lang dojo/dom-class dojo/dom-construct dojo/dom-geometry dojo/dom-style dojo/has dijit/_Contained dijit/_WidgetBase ./_css3".split(" "),function(p,h,q,f,c,g,k,r,l,m,n){var a=h("dojox.mobile.ProgressIndicator",[m,l],{interval:100,size:40,removeOnStop:!0,startSpinning:!1,center:!0,colors:null,baseClass:"mblProgressIndicator",constructor:function(){this.colors=[];this._bars=[]},buildRendering:function(){this.inherited(arguments);
this.center&&f.add(this.domNode,"mblProgressIndicatorCenter");this.containerNode=c.create("div",{className:"mblProgContainer"},this.domNode);this.spinnerNode=c.create("div",null,this.containerNode);for(var b=0;12>b;b++){var a=c.create("div",{className:"mblProg mblProg"+b},this.spinnerNode);this._bars.push(a)}this.scale(this.size);this.startSpinning&&this.start()},scale:function(b){var a=b/40;k.set(this.containerNode,n.add({},{transform:"scale("+a+")",transformOrigin:"0 0"}));g.setMarginBox(this.domNode,
{w:b,h:b});g.setMarginBox(this.containerNode,{w:b/a,h:b/a})},start:function(){if(this.imageNode){var b=this.imageNode,a=Math.round((this.containerNode.offsetWidth-b.offsetWidth)/2),c=Math.round((this.containerNode.offsetHeight-b.offsetHeight)/2);b.style.margin=c+"px "+a+"px"}else{var d=0,e=this;this.timer=setInterval(function(){d--;d=0>d?11:d;for(var b=e.colors,a=0;12>a;a++){var c=(d+a)%12;b[c]?e._bars[a].style.backgroundColor=b[c]:f.replace(e._bars[a],"mblProg"+c+"Color","mblProg"+(11===c?0:c+1)+
"Color")}},this.interval)}},stop:function(){this.timer&&clearInterval(this.timer);this.timer=null;this.removeOnStop&&(this.domNode&&this.domNode.parentNode)&&this.domNode.parentNode.removeChild(this.domNode)},setImage:function(a){a?(this.imageNode=c.create("img",{src:a},this.containerNode),this.spinnerNode.style.display="none"):(this.imageNode&&(this.containerNode.removeChild(this.imageNode),this.imageNode=null),this.spinnerNode.style.display="")},destroy:function(){this.inherited(arguments);this===
a._instance&&(a._instance=null)}});a._instance=null;a.getInstance=function(b){a._instance||(a._instance=new a(b));return a._instance};return a});
//# sourceMappingURL=ProgressIndicator.js.map