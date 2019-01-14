import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApproveTaskComponent } from 'app/tasks/approve-request/approve-task.component';
import { PageNotFoundComponent } from 'app/tasks/page-not-found/page-not-found.component';

const routes: Routes = [
  {
    path: 'tasks/amend-request/id/:taskId',
    component: ApproveTaskComponent,
  },
  {
    path: 'tasks/approve-request/id/:taskId',
    component: ApproveTaskComponent,
  },
  { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [
    RouterModule
  ],
  declarations: []
})

export class AppRoutingModule { }
