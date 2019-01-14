import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApprovalRequest } from 'process/model/approvalRequest';
import { AmendRequestService } from 'process/api/amendRequest.service';
import { Task } from 'process/model/task';
import { TaskAmendRequestSubmitData } from 'process/model/taskAmendRequestSubmitData';

@Component({
  selector: 'app-task-amend',
  templateUrl: './amend-task.component.html',
  styleUrls: [ '../tasks.component.scss' ]
})
export class AmendTaskComponent {

  task: Task = this.emptyTask();
  comment = '';
  submitData: TaskAmendRequestSubmitData = {
    action: '',
    approvalRequest: this.emptyApprovalRequest()
  };

  constructor(
    private client: AmendRequestService,
    route: ActivatedRoute
  ) {
    const taskId: string = route.snapshot.paramMap.get('taskId');
    this.client.loadTaskAmendRequestFormData(taskId).subscribe(
      formData => {
        this.task = formData.task;
        this.comment = formData.comment;
        this.submitData.approvalRequest = formData.approvalRequest;
      }, error => {
        console.log('Error loading amend request task with id', taskId);
      }
    );
  }

  complete() {
    console.log('Decision for', this.task.id, 'is', this.submitData.action);
    this.client.submitTaskAmendRequestSubmitData(this.task.id, this.submitData).subscribe(
      result => {
        console.log('Sucessfully submitted');
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
