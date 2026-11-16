import fs from 'node:fs/promises';
const root='backend/datos-ejemplo/marcas-imagenes';
const titles={Toyota:'Toyota carlogo.svg',Nissan:'Nissan 2020 logo.svg',Hyundai:'Hyundai Motor Company logo.svg',Kia:'KIA logo3.svg',Chevrolet:'Chevrolet-logo.png',Suzuki:'Suzuki logo 2.svg',Mitsubishi:'Mitsubishi motors new logo.svg',Honda:'Honda-logo.svg',Mazda:'Mazda logo.svg',Volkswagen:'Volkswagen logo 2019.svg'};
const result=[];
await Promise.all(Object.entries(titles).map(async([marca,file])=>{
 const url=new URL('https://commons.wikimedia.org/w/api.php');url.search=new URLSearchParams({action:'query',format:'json',prop:'imageinfo',iiprop:'url|extmetadata',iiurlwidth:'512',titles:'File:'+file});
 try{const r=await fetch(url,{signal:AbortSignal.timeout(20000)});if(!r.ok)throw Error(r.status);const j=await r.json();const page=Object.values(j.query.pages)[0];const info=page.imageinfo?.[0];result.push({marca,file,info});console.log(marca+': '+(info?.thumburl||info?.url||'NO ENCONTRADO'));}catch(e){result.push({marca,error:String(e)});console.log(marca+': '+e)}
}));await fs.writeFile(root+'/candidatas.json',JSON.stringify(result,null,2));
