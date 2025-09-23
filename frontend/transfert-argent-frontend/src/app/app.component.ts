import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';  // RouterModule pour gérer les routes
import { CoreModule } from './core.module'; 


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, CoreModule],  // Importer le RouterModule et CoreModule
  template: '<router-outlet></router-outlet>',  // Le router-outlet pour afficher les composants selon les routes
})
export class AppComponent {}
