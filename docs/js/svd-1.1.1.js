!function(r,f){"object"==typeof exports&&"undefined"!=typeof module?f(exports):"function"==typeof define&&define.amd?define(["exports"],f):f((r="undefined"!=typeof globalThis?globalThis:r||self).SVDJS={})}(this,function(r){"use strict";r.SVD=function(r,f,o,e,t){if(f=void 0===f||f,o=void 0===o||o,t=1e-64/(e=e||Math.pow(2,-52)),!r)throw new TypeError("Matrix a is not defined");var i,a,n,s,h,l,M,d,p,b,u,w,y=r[0].length,q=r.length;if(q<y)throw new TypeError("Invalid matrix: m < n");for(var v=[],c=[],x=[],g="f"===f?q:y,m=b=M=0;m<q;m++)c[m]=new Array(g).fill(0);for(m=0;m<y;m++)x[m]=new Array(y).fill(0);var S,T=new Array(y).fill(0);for(m=0;m<q;m++)for(i=0;i<y;i++)c[m][i]=r[m][i];for(m=0;m<y;m++){for(v[m]=M,p=0,n=m+1,i=m;i<q;i++)p+=Math.pow(c[i][m],2);if(p<t)M=0;else for(d=(l=c[m][m])*(M=l<0?Math.sqrt(p):-Math.sqrt(p))-p,c[m][m]=l-M,i=n;i<y;i++){for(p=0,a=m;a<q;a++)p+=c[a][m]*c[a][i];for(l=p/d,a=m;a<q;a++)c[a][i]=c[a][i]+l*c[a][m]}for(T[m]=M,p=0,i=n;i<y;i++)p+=Math.pow(c[m][i],2);if(p<t)M=0;else{for(d=(l=c[m][m+1])*(M=l<0?Math.sqrt(p):-Math.sqrt(p))-p,c[m][m+1]=l-M,i=n;i<y;i++)v[i]=c[m][i]/d;for(i=n;i<q;i++){for(p=0,a=n;a<y;a++)p+=c[i][a]*c[m][a];for(a=n;a<y;a++)c[i][a]=c[i][a]+p*v[a]}}b<(u=Math.abs(T[m])+Math.abs(v[m]))&&(b=u)}if(o)for(m=y-1;0<=m;m--){if(0!==M){for(d=c[m][m+1]*M,i=n;i<y;i++)x[i][m]=c[m][i]/d;for(i=n;i<y;i++){for(p=0,a=n;a<y;a++)p+=c[m][a]*x[a][i];for(a=n;a<y;a++)x[a][i]=x[a][i]+p*x[a][m]}}for(i=n;i<y;i++)x[m][i]=0,x[i][m]=0;x[m][m]=1,M=v[m],n=m}if(f){if("f"===f)for(m=y;m<q;m++){for(i=y;i<q;i++)c[m][i]=0;c[m][m]=1}for(m=y-1;0<=m;m--){for(n=m+1,M=T[m],i=n;i<g;i++)c[m][i]=0;if(0!==M){for(d=c[m][m]*M,i=n;i<g;i++){for(p=0,a=n;a<q;a++)p+=c[a][m]*c[a][i];for(l=p/d,a=m;a<q;a++)c[a][i]=c[a][i]+l*c[a][m]}for(i=m;i<q;i++)c[i][m]=c[i][m]/M}else for(i=m;i<q;i++)c[i][m]=0;c[m][m]=c[m][m]+1}}for(e*=b,a=y-1;0<=a;a--)for(var k=0;k<50;k++){for(S=!1,n=a;0<=n;n--){if(Math.abs(v[n])<=e){S=!0;break}if(Math.abs(T[n-1])<=e)break}if(!S)for(h=0,s=n-(p=1),m=n;m<a+1&&(l=p*v[m],v[m]=h*v[m],!(Math.abs(l)<=e));m++)if(M=T[m],T[m]=Math.sqrt(l*l+M*M),h=M/(d=T[m]),p=-l/d,f)for(i=0;i<q;i++)u=c[i][s],w=c[i][m],c[i][s]=u*h+w*p,c[i][m]=-u*p+w*h;if(w=T[a],n===a){if(w<0&&(T[a]=-w,o))for(i=0;i<y;i++)x[i][a]=-x[i][a];break}for(b=T[n],l=(((u=T[a-1])-w)*(u+w)+((M=v[a-1])-(d=v[a]))*(M+d))/(2*d*u),M=Math.sqrt(l*l+1),l=((b-w)*(b+w)+d*(u/(l<0?l-M:l+M)-d))/b,m=n+(p=h=1);m<a+1;m++){if(M=v[m],u=T[m],d=p*M,M*=h,w=Math.sqrt(l*l+d*d),l=b*(h=l/(v[m-1]=w))+M*(p=d/w),M=-b*p+M*h,d=u*p,u*=h,o)for(i=0;i<y;i++)b=x[i][m-1],w=x[i][m],x[i][m-1]=b*h+w*p,x[i][m]=-b*p+w*h;if(w=Math.sqrt(l*l+d*d),l=(h=l/(T[m-1]=w))*M+(p=d/w)*u,b=-p*M+h*u,f)for(i=0;i<q;i++)u=c[i][m-1],w=c[i][m],c[i][m-1]=u*h+w*p,c[i][m]=-u*p+w*h}v[n]=0,v[a]=l,T[a]=b}for(m=0;m<y;m++)T[m]<e&&(T[m]=0);return{u:c,q:T,v:x}},r.VERSION="1.1.1",Object.defineProperty(r,"__esModule",{value:!0})});
