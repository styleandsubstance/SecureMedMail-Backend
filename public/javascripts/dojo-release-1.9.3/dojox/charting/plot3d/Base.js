//>>built
define("dojox/charting/plot3d/Base",["dojo/_base/declare","dojo/has"],function(c,d){var b=c("dojox.charting.plot3d.Base",null,{constructor:function(a,b,c){this.width=a;this.height=b},setData:function(a){this.data=a?a:[];return this},getDepth:function(){return this.depth},generate:function(a,b){}});d("dojo-bidi")&&b.extend({_checkOrientation:function(a){a.isMirrored&&a.applyMirroring(a.view,{width:this.width,height:this.height},{l:0,r:0,t:0,b:0})}});return b});
//# sourceMappingURL=Base.js.map