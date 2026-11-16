import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { Subject, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LoginComponent } from './login';
import { AuthService } from '../../../_services/auth.service';
describe('Inicio de sesión',()=>{
 let auth:any;let retorno:string;let page:LoginComponent;
 beforeEach(()=>{retorno='/carrito';auth={login:vi.fn(()=>of({success:true,data:{}}))};TestBed.configureTestingModule({imports:[LoginComponent],providers:[provideRouter([]),{provide:AuthService,useValue:auth},{provide:ActivatedRoute,useValue:{snapshot:{queryParamMap:{get:()=>retorno}}}}]});page=TestBed.createComponent(LoginComponent).componentInstance;vi.spyOn(TestBed.inject(Router),'navigateByUrl').mockResolvedValue(true);});
 it('valida los datos y conserva el destino después de ingresar',()=>{page.onSubmit();expect(auth.login).not.toHaveBeenCalled();page.loginForm.setValue({correo:'ana@example.com',contrasena:'Anterior123'});page.onSubmit();expect(TestBed.inject(Router).navigateByUrl).toHaveBeenCalledWith('/carrito');});
 it('impide redirecciones externas',()=>{retorno='//otro.example.com';page.loginForm.setValue({correo:'ana@example.com',contrasena:'Anterior123'});page.onSubmit();expect(TestBed.inject(Router).navigateByUrl).toHaveBeenCalledWith('/');});
 it('evita iniciar sesión dos veces mientras espera',()=>{auth.login.mockReturnValueOnce(new Subject());page.loginForm.setValue({correo:'ana@example.com',contrasena:'Anterior123'});page.onSubmit();page.onSubmit();expect(auth.login).toHaveBeenCalledTimes(1);});
});
