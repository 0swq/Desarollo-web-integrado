import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { RegisterComponent } from './register';
import { AuthService } from '../../../_services/auth.service';
describe('Registro de cuenta',()=>{
 let auth:any;let page:RegisterComponent;
 beforeEach(()=>{auth={registrar:vi.fn(()=>of({success:true}))};TestBed.configureTestingModule({imports:[RegisterComponent],providers:[provideRouter([]),{provide:AuthService,useValue:auth},{provide:ActivatedRoute,useValue:{snapshot:{queryParamMap:{get:()=>'/carrito'}}}}]});page=TestBed.createComponent(RegisterComponent).componentInstance;vi.spyOn(TestBed.inject(Router),'navigate').mockResolvedValue(true);page.regForm.setValue({nombre:' Ana ',apellido:' Perez ',correo:'ana@example.com',telefono:'940061944',direccion:'Chimbote',contrasena:'Nueva123456',repeatPass:'Nueva123456'});});
 it('envía todos los campos personales sin confirmación ni rol',()=>{page.onSubmit();expect(auth.registrar).toHaveBeenCalledWith({nombre:'Ana',apellido:'Perez',correo:'ana@example.com',telefono:'940061944',direccion:'Chimbote',contrasena:'Nueva123456'});expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/iniciar-sesion'],{queryParams:{returnUrl:'/carrito'}});});
 it('vuelve a comprobar la confirmación al modificar la contraseña original',()=>{page.form.contrasena.setValue('Diferente123');page.onSubmit();expect(auth.registrar).not.toHaveBeenCalled();expect(page.regForm.hasError('notSame')).toBe(true);});
 it('permite omitir teléfono y dirección',()=>{page.form.telefono.setValue('');page.form.direccion.setValue('');page.onSubmit();expect(auth.registrar).toHaveBeenCalledTimes(1);});
 it('mantiene el formulario ante errores del servidor',()=>{auth.registrar.mockReturnValueOnce(throwError(()=>({error:{message:'Correo registrado'}})));page.onSubmit();expect(page.errorMensaje).toBe('Correo registrado');expect(page.cargando).toBe(false);});
});
