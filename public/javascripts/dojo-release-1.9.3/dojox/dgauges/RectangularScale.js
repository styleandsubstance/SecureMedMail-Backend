//>>built
define("dojox/dgauges/RectangularScale",["dojo/_base/declare","dojox/gfx","./ScaleBase"],function(r,h,s){return r("dojox.dgauges.RectangularScale",s,{paddingLeft:15,paddingTop:12,paddingRight:15,paddingBottom:0,_contentBox:null,constructor:function(){this.labelPosition="leading";this.addInvalidatingProperties(["paddingTop","paddingLeft","paddingRight","paddingBottom"])},positionForValue:function(b){var a=0,c=0,d=0;this._contentBox&&("horizontal"==this._gauge.orientation?(c=this._contentBox.x,d=this._contentBox.w):
(c=this._contentBox.y,d=this._contentBox.h));a=this.scaler.positionForValue(b);return c+a*d},valueForPosition:function(b){var a=this.scaler.minimum,a=NaN,c=0,d=0;"horizontal"==this._gauge.orientation?(a=b.x,c=this._contentBox.x,d=this._contentBox.x+this._contentBox.w):(a=b.y,c=this._contentBox.y,d=this._contentBox.y+this._contentBox.h);return a=a<=c?this.scaler.minimum:a>=d?this.scaler.maximum:this.scaler.valueForPosition((a-c)/(d-c))},refreshRendering:function(){this.inherited(arguments);if(this._gfxGroup&&
this.scaler){this._ticksGroup.clear();var b=this._gauge._layoutInfos.middle;this._contentBox={};this._contentBox.x=b.x+this.paddingLeft;this._contentBox.y=b.y+this.paddingTop;this._contentBox.w=b.w-(this.paddingLeft+this.paddingRight);this._contentBox.h=b.h-(this.paddingBottom+this.paddingTop);for(var a,b=this._getFont(),c=this.scaler.computeTicks(),d=0;d<c.length;d++){var k=c[d];if(a=this.tickShapeFunc(this._ticksGroup,this,k)){var e=this.positionForValue(k.value),p=this._gauge._computeBoundingBox(a).width,
f=0,g=0,l=0;"horizontal"==this._gauge.orientation?(f=e,g=this._contentBox.y,l=90):(f=this._contentBox.x,g=e);a.setTransform([{dx:f,dy:g},h.matrix.rotateg(l)])}if(a=this.tickLabelFunc(k)){var e=h._base._getTextBox(a,{font:h.makeFontString(h.makeParameters(h.defaultFont,b))}),k=e.w,e=e.h,l="start",m=f,n=g;"horizontal"==this._gauge.orientation?(m=f,n="trailing"==this.labelPosition?g+p+this.labelGap+e:g-this.labelGap,l="middle"):(m="trailing"==this.labelPosition?f+p+this.labelGap:f-this.labelGap-k,n=
g+e/2);a=this._ticksGroup.createText({x:m,y:n,text:a,align:l});a.setFill(b.color?b.color:"black");a.setFont(b)}}for(var q in this._indicatorsIndex)this._indicatorsRenderers[q]=this._indicatorsIndex[q].invalidateRendering()}}})});
//# sourceMappingURL=RectangularScale.js.map