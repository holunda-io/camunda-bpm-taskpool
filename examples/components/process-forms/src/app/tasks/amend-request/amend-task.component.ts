import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EnvironmentHelperService } from 'app/services/environment.helper.service';
import { UserTaskAmendRequestService } from 'process/services/user-task-amend-request.service';
import { Environment } from 'process/models/environment';
import { TaskAmendRequestSubmitData } from 'process/models/task-amend-request-submit-data';
import { ApprovalRequest } from 'process/models/approval-request';
import { Task } from 'process/models/task';

@Component({
  selector: 'app-task-amend',
  templateUrl: './amend-task.component.html',
  styleUrls: [ '../tasks.component.scss' ]
})
export class AmendTaskComponent {

  constructor(
    private client: UserTaskAmendRequestService,
    private envProvider: EnvironmentHelperService,
    private router: Router,
    route: ActivatedRoute
  ) {
    this.userId = route.snapshot.queryParams['userId'];
    const taskId: string = route.snapshot.paramMap.get('taskId');
    this.client.loadTaskAmendRequestFormData({ id: taskId, 'X-Current-User-ID': this.userId}).subscribe(
      formData => {
        this.task = formData.task;
        this.comment = formData.comment;
        this.submitData.approvalRequest = formData.approvalRequest;
      }, error => {
        console.log('Error loading amend request task with id', taskId);
      }
    );
    this.envProvider.env().subscribe(e => this.environment = e);
  }

  task: Task = AmendTaskComponent.emptyTask();
  userId: string;
  environment: Environment = this.envProvider.none();
  comment = '';
  submitData: TaskAmendRequestSubmitData = {
    action: '',
    approvalRequest: AmendTaskComponent.emptyApprovalRequest()
  };

  private static emptyTask(): Task {
    return {
      id: 'undefined',
      createTime: null,
      dueDate: null,
      name: 'undefined',
      description: 'undefined'
    };
  }

  private static emptyApprovalRequest(): ApprovalRequest {
    return {
      applicant: '',
      amount: 0.00,
      currency: 'USD',
      id: 'undefined',
      subject: ''
    };
  }

  complete() {
    console.log('Decision for', this.task.id, 'is', this.submitData.action);
    this.client.submitTaskAmendRequestSubmitData({ id: this.task.id, 'X-Current-User-ID': this.userId, body: this.submitData}).subscribe(
      result => {
        console.log('Sucessfully submitted');
        this.router.navigate(['/externalRedirect', { externalUrl: this.environment.tasklistUrl }], {
          skipLocationChange: true,
        });
      }, error => {
        console.log('Error submitting amend request task with id', this.task.id);
      }
    );
  }
}
