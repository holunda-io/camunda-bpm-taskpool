import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApprovalRequest } from 'process/model/approvalRequest';
import { AmendRequestService } from 'process/api/amendRequest.service';
import { Task } from 'process/model/task';
import { TaskAmendRequestSubmitData } from 'process/model/taskAmendRequestSubmitData';
import { EnvironmentHelperService } from 'app/services/environment.helper.service';
import { Environment } from 'process/model/environment';

@Component({
  selector: 'app-task-amend',
  templateUrl: './amend-task.component.html',
  styleUrls: [ '../tasks.component.scss' ]
})
export class AmendTaskComponent {

  task: Task = this.emptyTask();
  environment: Environment = this.envProvider.none();
  comment = '';
  submitData: TaskAmendRequestSubmitData = {
    action: '',
    approvalRequest: this.emptyApprovalRequest()
  };

  constructor(
    private client: AmendRequestService,
    private envProvider: EnvironmentHelperService,
    private router: Router,
    route: ActivatedRoute
  ) {
    const userId = 'irnoman'; // FIXME
    const taskId: string = route.snapshot.paramMap.get('taskId');
    this.client.loadTaskAmendRequestFormData(taskId, userId).subscribe(
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

  complete() {
    const userId = 'irnoman'; // FIXME
    console.log('Decision for', this.task.id, 'is', this.submitData.action);
    this.client.submitTaskAmendRequestSubmitData(this.task.id, userId, this.submitData).subscribe(
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

  emptyTask(): Task {
    return {
      id: 'undefined',
      createTime: null,
      dueDate: null,
      name: 'undefined',
      description: 'undefined'
    };
  }

  emptyApprovalRequest(): ApprovalRequest {
    return {
      applicant: '',
      amount: '',
      id: 'undefined',
      subject: ''
    };
  }
}
