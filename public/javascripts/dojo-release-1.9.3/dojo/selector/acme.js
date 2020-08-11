//>>built
define("dojo/selector/acme",["../dom","../sniff","../_base/array","../_base/lang","../_base/window"],function(L,m,M,$,y){var N=$.trim,I=M.forEach,O="BackCompat"==y.doc.compatMode,t=!1,z=function(){return!0},J=function(b){b=0<="\x3e~+".indexOf(b.slice(-1))?b+" * ":b+" ";for(var a=function(a,c){return N(b.slice(a,c))},c=[],d=-1,f=-1,e=-1,h=-1,r=-1,q=-1,A=-1,E,B="",l="",m,k=0,s=b.length,g=null,n=null,p=function(){0<=q&&(g.id=a(q,k).replace(/\\/g,""),q=-1);if(0<=A){var b=A==k?null:a(A,k);g[0>"\x3e~+".indexOf(b)?
"tag":"oper"]=b;A=-1}0<=r&&(g.classes.push(a(r+1,k).replace(/\\/g,"")),r=-1)};B=l,l=b.charAt(k),k<s;k++)if("\\"!=B)if(g||(m=k,g={query:null,pseudos:[],attrs:[],classes:[],tag:null,oper:null,id:null,getTag:function(){return t?this.otag:this.tag}},A=k),E)l==E&&(E=null);else if("'"==l||'"'==l)E=l;else if(0<=d)if("]"==l){n.attr?n.matchFor=a(e||d+1,k):n.attr=a(d+1,k);if((d=n.matchFor)&&('"'==d.charAt(0)||"'"==d.charAt(0)))n.matchFor=d.slice(1,-1);n.matchFor&&(n.matchFor=n.matchFor.replace(/\\/g,""));g.attrs.push(n);
n=null;d=e=-1}else"\x3d"==l&&(e=0<="|~^$*".indexOf(B)?B:"",n.type=e+l,n.attr=a(d+1,k-e.length),e=k+1);else 0<=f?")"==l&&(0<=h&&(n.value=a(f+1,k)),h=f=-1):"#"==l?(p(),q=k+1):"."==l?(p(),r=k):":"==l?(p(),h=k):"["==l?(p(),d=k,n={}):"("==l?(0<=h&&(n={name:a(h+1,k),value:null},g.pseudos.push(n)),f=k):" "==l&&B!=l&&(p(),0<=h&&g.pseudos.push({name:a(h+1,k)}),g.loops=g.pseudos.length||g.attrs.length||g.classes.length,g.oquery=g.query=a(m,k),g.otag=g.tag=g.oper?null:g.tag||"*",g.tag&&(g.tag=g.tag.toUpperCase()),
c.length&&c[c.length-1].oper&&(g.infixOper=c.pop(),g.query=g.infixOper.query+" "+g.query),c.push(g),g=null);return c},u=function(b,a){return!b?a:!a?b:function(){return b.apply(window,arguments)&&a.apply(window,arguments)}},v=function(b,a){var c=a||[];b&&c.push(b);return c},F=function(b){return 1==b.nodeType},w=function(b,a){return!b?"":"class"==a?b.className||"":"for"==a?b.htmlFor||"":"style"==a?b.style.cssText||"":(t?b.getAttribute(a):b.getAttribute(a,2))||""},P={"*\x3d":function(b,a){return function(c){return 0<=
w(c,b).indexOf(a)}},"^\x3d":function(b,a){return function(c){return 0==w(c,b).indexOf(a)}},"$\x3d":function(b,a){return function(c){c=" "+w(c,b);var d=c.lastIndexOf(a);return-1<d&&d==c.length-a.length}},"~\x3d":function(b,a){var c=" "+a+" ";return function(a){return 0<=(" "+w(a,b)+" ").indexOf(c)}},"|\x3d":function(b,a){var c=a+"-";return function(d){d=w(d,b);return d==a||0==d.indexOf(c)}},"\x3d":function(b,a){return function(c){return w(c,b)==a}}},G="undefined"==typeof y.doc.firstChild.nextElementSibling,
C=!G?"nextElementSibling":"nextSibling",aa=!G?"previousElementSibling":"previousSibling",D=G?F:z,Q=function(b){for(;b=b[aa];)if(D(b))return!1;return!0},R=function(b){for(;b=b[C];)if(D(b))return!1;return!0},H=function(b){var a=b.parentNode,a=7!=a.nodeType?a:a.nextSibling,c=0,d=a.children||a.childNodes,f=b._i||b.getAttribute("_i")||-1,e=a._l||("undefined"!==typeof a.getAttribute?a.getAttribute("_l"):-1);if(!d)return-1;d=d.length;if(e==d&&0<=f&&0<=e)return f;m("ie")&&"undefined"!==typeof a.setAttribute?
a.setAttribute("_l",d):a._l=d;f=-1;for(a=a.firstElementChild||a.firstChild;a;a=a[C])D(a)&&(m("ie")?a.setAttribute("_i",++c):a._i=++c,b===a&&(f=c));return f},ba=function(b){return!(H(b)%2)},ca=function(b){return H(b)%2},S={checked:function(b,a){return function(a){return!!("checked"in a?a.checked:a.selected)}},disabled:function(b,a){return function(a){return a.disabled}},enabled:function(b,a){return function(a){return!a.disabled}},"first-child":function(){return Q},"last-child":function(){return R},
"only-child":function(b,a){return function(a){return Q(a)&&R(a)}},empty:function(b,a){return function(a){var b=a.childNodes;for(a=a.childNodes.length-1;0<=a;a--){var f=b[a].nodeType;if(1===f||3==f)return!1}return!0}},contains:function(b,a){var c=a.charAt(0);if('"'==c||"'"==c)a=a.slice(1,-1);return function(b){return 0<=b.innerHTML.indexOf(a)}},not:function(b,a){var c=J(a)[0],d={el:1};"*"!=c.tag&&(d.tag=1);c.classes.length||(d.classes=1);var f=s(c,d);return function(a){return!f(a)}},"nth-child":function(b,
a){var c=parseInt;if("odd"==a)return ca;if("even"==a)return ba;if(-1!=a.indexOf("n")){var d=a.split("n",2),f=d[0]?"-"==d[0]?-1:c(d[0]):1,e=d[1]?c(d[1]):0,h=0,r=-1;0<f?0>e?e=e%f&&f+e%f:0<e&&(e>=f&&(h=e-e%f),e%=f):0>f&&(f*=-1,0<e&&(r=e,e%=f));if(0<f)return function(a){a=H(a);return a>=h&&(0>r||a<=r)&&a%f==e};a=e}var q=c(a);return function(a){return H(a)==q}}},da=9>m("ie")||9==m("ie")&&m("quirks")?function(b){var a=b.toLowerCase();"class"==a&&(b="className");return function(c){return t?c.getAttribute(b):
c[b]||c[a]}}:function(b){return function(a){return a&&a.getAttribute&&a.hasAttribute(b)}},s=function(b,a){if(!b)return z;a=a||{};var c=null;"el"in a||(c=u(c,F));"tag"in a||"*"!=b.tag&&(c=u(c,function(a){return a&&(t?a.tagName:a.tagName.toUpperCase())==b.getTag()}));"classes"in a||I(b.classes,function(a,b,e){var h=RegExp("(?:^|\\s)"+a+"(?:\\s|$)");c=u(c,function(a){return h.test(a.className)});c.count=b});"pseudos"in a||I(b.pseudos,function(a){var b=a.name;S[b]&&(c=u(c,S[b](b,a.value)))});"attrs"in
a||I(b.attrs,function(a){var b,e=a.attr;a.type&&P[a.type]?b=P[a.type](e,a.matchFor):e.length&&(b=da(e));b&&(c=u(c,b))});"id"in a||b.id&&(c=u(c,function(a){return!!a&&a.id==b.id}));c||"default"in a||(c=z);return c},ea=function(b){return function(a,c,d){for(;a=a[C];)if(!G||F(a)){(!d||x(a,d))&&b(a)&&c.push(a);break}return c}},fa=function(b){return function(a,c,d){for(a=a[C];a;){if(D(a)){if(d&&!x(a,d))break;b(a)&&c.push(a)}a=a[C]}return c}},ga=function(b){b=b||z;return function(a,c,d){for(var f=0,e=a.children||
a.childNodes;a=e[f++];)D(a)&&((!d||x(a,d))&&b(a,f))&&c.push(a);return c}},T={},U=function(b){var a=T[b.query];if(a)return a;var c=b.infixOper,c=c?c.oper:"",d=s(b,{el:1}),f="*"==b.tag,e=y.doc.getElementsByClassName;if(c)e={el:1},f&&(e.tag=1),d=s(b,e),"+"==c?a=ea(d):"~"==c?a=fa(d):"\x3e"==c&&(a=ga(d));else if(b.id)d=!b.loops&&f?z:s(b,{el:1,id:1}),a=function(a,c){var f=L.byId(b.id,a.ownerDocument||a);if(f&&d(f)){if(9==a.nodeType)return v(f,c);for(var e=f.parentNode;e&&e!=a;)e=e.parentNode;if(e)return v(f,
c)}};else if(e&&/\{\s*\[native code\]\s*\}/.test(String(e))&&b.classes.length&&!O)var d=s(b,{el:1,classes:1,id:1}),h=b.classes.join(" "),a=function(a,b,c){b=v(0,b);for(var f,e=0,l=a.getElementsByClassName(h);f=l[e++];)d(f,a)&&x(f,c)&&b.push(f);return b};else!f&&!b.loops?a=function(a,c,d){c=v(0,c);for(var f=0,e=b.getTag(),e=e?a.getElementsByTagName(e):[];a=e[f++];)x(a,d)&&c.push(a);return c}:(d=s(b,{el:1,tag:1,id:1}),a=function(a,c,f){c=v(0,c);for(var e,h=0,l=(e=b.getTag())?a.getElementsByTagName(e):
[];e=l[h++];)d(e,a)&&x(e,f)&&c.push(e);return c});return T[b.query]=a},V={},W={},X=function(b){var a=J(N(b));if(1==a.length){var c=U(a[0]);return function(a){if(a=c(a,[]))a.nozip=!0;return a}}return function(b){b=v(b);for(var c,e,h=a.length,r,q,m=0;m<h;m++){q=[];c=a[m];e=b.length-1;0<e&&(r={},q.nozip=!0);e=U(c);for(var p=0;c=b[p];p++)e(c,q,r);if(!q.length)break;b=q}return q}},ha=m("ie")?"commentStrip":"nozip",Y=!!y.doc.querySelectorAll,ia=/\\[>~+]|n\+\d|([^ \\])?([>~+])([^ =])?/g,ja=function(b,a,
c,d){return c?(a?a+" ":"")+c+(d?" "+d:""):b},ka=/([^[]*)([^\]]*])?/g,la=function(b,a,c){return a.replace(ia,ja)+(c||"")},Z=function(b,a){b=b.replace(ka,la);if(Y){var c=W[b];if(c&&!a)return c}if(c=V[b])return c;var c=b.charAt(0),d=-1==b.indexOf(" ");0<=b.indexOf("#")&&d&&(a=!0);if(Y&&!a&&-1=="\x3e~+".indexOf(c)&&(!m("ie")||-1==b.indexOf(":"))&&!(O&&0<=b.indexOf("."))&&-1==b.indexOf(":contains")&&-1==b.indexOf(":checked")&&-1==b.indexOf("|\x3d")){var f=0<="\x3e~+".indexOf(b.charAt(b.length-1))?b+" *":
b;return W[b]=function(a){try{if(!(9==a.nodeType||d))throw"";var c=a.querySelectorAll(f);c[ha]=!0;return c}catch(e){return Z(b,!0)(a)}}}var e=b.match(/([^\s,](?:"(?:\\.|[^"])+"|'(?:\\.|[^'])+'|[^,])*)/g);return V[b]=2>e.length?X(b):function(a){for(var b=0,c=[],d;d=e[b++];)c=c.concat(X(d)(a));return c}},p=0,ma=m("ie")?function(b){return t?b.getAttribute("_uid")||b.setAttribute("_uid",++p)||p:b.uniqueID}:function(b){return b._uid||(b._uid=++p)},x=function(b,a){if(!a)return 1;var c=ma(b);return!a[c]?
a[c]=1:0},na=function(b){if(b&&b.nozip)return b;if(!b||!b.length)return[];if(2>b.length)return[b[0]];var a=[];p++;var c,d;if(m("ie")&&t){var f=p+"";for(c=0;c<b.length;c++)if((d=b[c])&&d.getAttribute("_zipIdx")!=f)a.push(d),d.setAttribute("_zipIdx",f)}else if(m("ie")&&b.commentStrip)try{for(c=0;c<b.length;c++)(d=b[c])&&F(d)&&a.push(d)}catch(e){}else for(c=0;c<b.length;c++)if((d=b[c])&&d._zipIdx!=p)a.push(d),d._zipIdx=p;return a},K=function(b,a){a=a||y.doc;t="div"===(a.ownerDocument||a).createElement("div").tagName;
var c=Z(b)(a);return c&&c.nozip?c:na(c)};K.filter=function(b,a,c){for(var d=[],f=J(a),f=1==f.length&&!/[^\w#\.]/.test(a)?s(f[0]):function(b){return-1!=M.indexOf(K(a,L.byId(c)),b)},e=0,h;h=b[e];e++)f(h)&&d.push(h);return d};return K});
//# sourceMappingURL=acme.js.map