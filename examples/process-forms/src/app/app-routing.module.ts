import { NgModule, InjectionToken } from '@angular/core';
import { RouterModule, Routes, ActivatedRouteSnapshot } from '@angular/router';
import { ApproveTaskComponent } from 'app/tasks/approve-request/approve-task.component';
import { AmendTaskComponent } from './tasks/amend-request/amend-task.component';
import { PageNotFoundComponent } from 'app/tasks/page-not-found/page-not-found.component';

const externalUrlProvider = new InjectionToken('externalUrlRedirectResolver');
const deactivateGuard = new InjectionToken('deactivateGuard');

const routes: Routes = [
  {
    path: 'tasks/amend-request/id/:taskId',
    component: AmendTaskComponent,
  },
  {
    path: 'tasks/approve-request/id/:taskId',
    component: ApproveTaskComponent,
  },
  {
    path: 'externalRedirect',
    canActivate: [externalUrlProvider],
    // We need a component here because we cannot define the route otherwise
    component: PageNotFoundComponent,
  },
  {
    path: '**',
    component: PageNotFoundComponent
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [
    RouterModule
  ],
  providers: [
    {
      provide: externalUrlProvider,
      useValue: (route: ActivatedRouteSnapshot) => {

        const externalUrl = route.paramMap.get('externalUrl');
        window.open(externalUrl, '_self');
      },
    },
    {
      provide: deactivateGuard,
      useValue: () => {
        console.log('Guard function is called!');
        return false;
      }
    },
  ],
  declarations: []
})

export class AppRoutingModule { }
