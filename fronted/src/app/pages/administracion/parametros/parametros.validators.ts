import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
export function texto(min:number,max:number,opcional=false):ValidatorFn{return(c:AbstractControl):ValidationErrors|null=>{
 const value=String(c.value ?? '').trim();if(!value)return opcional?null:{required:true};
 if(value.length<min)return{minlength:{requiredLength:min}};if(value.length>max)return{maxlength:{requiredLength:max}};
 return /[\x00-\x1f\x7f]/.test(value)?{caracteres:true}:null;
};}
export const telefonoValido:ValidatorFn=c=>{const value=String(c.value ?? '').trim();if(!value)return{required:true};const digitos=value.replace(/\D/g,'');return /^\+?[0-9 ()-]+$/.test(value) && digitos.length>=7 && digitos.length<=15 && value.length<=30?null:{telefono:true};};
export const coordenadaValida:ValidatorFn=c=>{if(c.value===null || c.value==='')return{required:true};return typeof c.value==='number' && Number.isFinite(c.value) && Number(c.value.toFixed(7))===c.value?null:{coordenada:true};};
export const correoValido:ValidatorFn=c=>{const v=String(c.value ?? '').trim();return !v || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)?null:{email:true};};
export const mapaValido:ValidatorFn=c=>{const v=String(c.value ?? '').trim();if(!v)return null;
 try{const u=new URL(v);if(!v.startsWith('https://') || v.includes(String.fromCharCode(92)) || u.protocol!=='https:' || u.username || u.password || /^https:\/\/[^/?#]*:/.test(v) || /\s/.test(v))return{maps:true};
  const host=u.hostname.toLowerCase();const path=u.pathname;
  if((['google.com','www.google.com'].includes(host) && (path==='/maps' || path.startsWith('/maps/'))) || host==='maps.google.com' || (host==='maps.app.goo.gl' && path.length>1) || (host==='goo.gl' && path.startsWith('/maps/') && path.length>6))return null;
 }catch{}return{maps:true};
};
