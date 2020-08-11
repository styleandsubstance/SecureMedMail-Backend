//>>built
define("dojox/charting/action2d/Tooltip","dijit/Tooltip dojo/_base/lang dojo/_base/declare dojo/_base/window dojo/_base/connect dojo/dom-style ./PlotAction dojox/gfx/matrix dojo/has dojo/has!dojo-bidi?../bidi/action2d/Tooltip dojox/lang/functional dojox/lang/functional/scan dojox/lang/functional/fold".split(" "),function(l,p,m,q,r,h,s,t,k,u,e){var n=function(a,b){var c=a.run&&a.run.data&&a.run.data[a.index];return c&&"number"!=typeof c&&(c.tooltip||c.text)?c.tooltip||c.text:b.tooltipFunc?b.tooltipFunc(a):
a.y},g=Math.PI/4,v=Math.PI/2;h=m(k("dojo-bidi")?"dojox.charting.action2d.NonBidiTooltip":"dojox.charting.action2d.Tooltip",s,{defaultParams:{text:n,mouseOver:!0},optionalParams:{},constructor:function(a,b,c){this.text=c&&c.text?c.text:n;this.mouseOver=c&&void 0!=c.mouseOver?c.mouseOver:!0;this.connect()},process:function(a){if("onplotreset"===a.type||"onmouseout"===a.type)l.hide(this.aroundRect),this.aroundRect=null,"onplotreset"===a.type&&delete this.angles;else if(a.shape&&!(this.mouseOver&&"onmouseover"!==
a.type||!this.mouseOver&&"onclick"!==a.type)){var b={type:"rect"},c=["after-centered","before-centered"];switch(a.element){case "marker":b.x=a.cx;b.y=a.cy;b.w=b.h=1;break;case "circle":b.x=a.cx-a.cr;b.y=a.cy-a.cr;b.w=b.h=2*a.cr;break;case "spider_circle":b.x=a.cx;b.y=a.cy;b.w=b.h=1;break;case "spider_plot":return;case "column":c=["above-centered","below-centered"];case "bar":b=p.clone(a.shape.getShape());b.w=b.width;b.h=b.height;break;case "candlestick":b.x=a.x;b.y=a.y;b.w=a.width;b.h=a.height;break;
default:this.angles||(this.angles="number"==typeof a.run.data[0]?e.map(e.scanl(a.run.data,"+",0),"* 2 * Math.PI / this",e.foldl(a.run.data,"+",0)):e.map(e.scanl(a.run.data,"a + b.y",0),"* 2 * Math.PI / this",e.foldl(a.run.data,"a + b.y",0)));var f=t._degToRad(a.plot.opt.startAngle),d=(this.angles[a.index]+this.angles[a.index+1])/2+f;b.x=a.cx+a.cr*Math.cos(d);b.y=a.cy+a.cr*Math.sin(d);b.w=b.h=1;if(f&&(0>d||d>2*Math.PI))d=Math.abs(2*Math.PI-Math.abs(d));d<g||(d<v+g?c=["below-centered","above-centered"]:
d<Math.PI+g?c=["before-centered","after-centered"]:d<2*Math.PI-g&&(c=["above-centered","below-centered"]))}k("dojo-bidi")&&this._recheckPosition(a,b,c);f=this.chart.getCoords();b.x+=f.x;b.y+=f.y;b.x=Math.round(b.x);b.y=Math.round(b.y);b.w=Math.ceil(b.w);b.h=Math.ceil(b.h);this.aroundRect=b;(a=this.text(a,this.plot))&&l.show(this._format(a),this.aroundRect,c);this.mouseOver||(this._handle=r.connect(q.doc,"onclick",this,"onClick"))}},onClick:function(){this.process({type:"onmouseout"})},_recheckPosition:function(a,
b,c){},_format:function(a){return a}});return k("dojo-bidi")?m("dojox.charting.action2d.Tooltip",[h,u]):h});
//# sourceMappingURL=Tooltip.js.map