import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApprovalRequest } from 'process/model/approvalRequest';
import { ApproveRequestService } from 'process/api/approveRequest.service';
import { Task } from 'process/model/task';
import { TaskApproveRequestSubmitData } from 'process/model/taskApproveRequestSubmitData';
import { EnvironmentHelperService } from 'app/services/environment.helper.service';
import { Environment } from 'process/model/environment';

@Component({
  selector: 'app-task-approve',
  templateUrl: './approve-task.component.html',
  styleUrls: [ '../tasks.component.scss' ]
})
export class ApproveTaskComponent {

  task: Task = ApproveTaskComponent.emptyTask();
  environment: Environment;
  userId: string;
  approvalRequest: ApprovalRequest = ApproveTaskComponent.emptyApprovalRequest();
  submitData: TaskApproveRequestSubmitData = {
    decision: ''
  };

  constructor(
    private client: ApproveRequestService,
    route: ActivatedRoute,
    private router: Router,
    private envProvider: EnvironmentHelperService
  ) {
    const taskId: string = route.snapshot.paramMap.get('taskId');
    this.userId = route.snapshot.queryParams['userId'];

    this.client.loadTaskApproveRequestFormData(taskId, this.userId).subscribe(
      formData => {
        this.task = formData.task;
        this.approvalRequest = formData.approvalRequest;
      }, error => {
        console.log('Error loading approve request task with id', taskId);
      }
    );
    this.envProvider.env().subscribe(e => {
      this.environment = e;
      console.log(this.environment);
    });
  }

  complete() {
    console.log('Decision for', this.task.id, 'is', this.submitData.decision);
    this.client.submitTaskApproveRequestSubmitData(this.task.id, this.userId, this.submitData).subscribe(
      result => {
        console.log('Sucessfully submitted');
        this.router.navigate(['/externalRedirect', { externalUrl: this.environment.tasklistUrl }], {
          skipLocationChange: true,
        });
      }, error => {
        console.log('Error submitting approve request task with id', this.task.id);
      }
    );
  }

  cancel() {
    window.close();
  }

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
      amount: '',
      id: 'undefined',
      subject: '',
      currency: 'EUR'
    };
  }
}
