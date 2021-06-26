import { NgModule, InjectionToken } from '@angular/core';
import { RouterModule, Routes, ActivatedRouteSnapshot } from '@angular/router';
import { PageNotFoundComponent } from 'app/components/page-not-found/page-not-found.component';
import {TasklistComponent} from 'app/task/tasklist/tasklist.component';
import {DataentryListComponent} from 'app/dataentry/dataentry-list/dataentry-list.component';

const externalUrlProvider = new InjectionToken('externalUrlRedirectResolver');
const deactivateGuard = new InjectionToken('deactivateGuard');

const routes: Routes = [
  {
    path: 'externalRedirect',
    canActivate: [externalUrlProvider],
    // We need a component here because we cannot define the route otherwise
    component: PageNotFoundComponent,
  },
  { path: 'tasks', component: TasklistComponent },
  { path: 'archive', component: DataentryListComponent },
  { path: '', redirectTo: 'tasks', pathMatch: 'full'}
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })
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
        console.log('Guard function is called.');
        return false;
      }
    },
  ],
  declarations: []
})

export class AppRoutingModule { }
