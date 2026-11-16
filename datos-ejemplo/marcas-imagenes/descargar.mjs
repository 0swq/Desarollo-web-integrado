import fs from 'node:fs/promises';
const root='backend/datos-ejemplo/marcas-imagenes';const data=JSON.parse(await fs.readFile(root+'/candidatas.json','utf8'));
await fs.mkdir(root+'/originales',{recursive:true});
await Promise.all(data.map(async a=>{if(!a.info)throw Error('Falta imagen '+a.marca);const source=a.info.thumburl||a.info.url;const r=await fetch(source,{signal:AbortSignal.timeout(30000)});if(!r.ok)throw Error(a.marca+' '+r.status);const b=Buffer.from(await r.arrayBuffer());if(b[0]!==137||b.subarray(1,4).toString()!=='PNG')throw Error('Se esperaba PNG: '+a.marca);a.archivo='originales/'+a.marca.toLowerCase()+'.png';a.contentType='image/png';a.fuente=source;a.size=b.length;await fs.writeFile(root+'/'+a.archivo,b);console.log('Logo preparado: '+a.marca+' ('+b.length+' bytes)')}));
await fs.writeFile(root+'/seleccion.json',JSON.stringify(data,null,2));
