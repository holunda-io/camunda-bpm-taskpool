import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import * as ApprovalRequestDraftSamples from 'app/data/approval-request-draft';
import {ApprovalRequestDraft} from 'process/model/approvalRequestDraft';
@Component({
  selector: 'app-request',
  templateUrl: './request.component.html',
  styleUrls: []
})
export class RequestComponent implements OnInit {

  @Input()
  approvalRequest: ApprovalRequestDraft;

  @Output()
  approvalRequestChange = new EventEmitter<ApprovalRequestDraft>();

  @Output()
  isValid = new EventEmitter<Object>();

  approvalForm: FormGroup;

  constructor(private formBuilder: FormBuilder) {
  }

  ngOnInit(): void {
    this.approvalForm = this.formBuilder.group({
      applicant: ['', Validators.required],
      subject: ['', Validators.required],
      amount: ['', Validators.required],
      currency: ['', Validators.required],
    });

    this.approvalForm.valueChanges.subscribe((changes) => {
      this.onChanges();
    });

    this.onChanges();
  }


  onChanges(): void {
    this.isValid.emit({
      valid: this.approvalForm.valid
    });

  }

  setApprovalRequest(approvalRequestDraft: ApprovalRequestDraft) {

    this.approvalForm.get('applicant').setValue(approvalRequestDraft.applicant);
    this.approvalForm.get('currency').setValue(approvalRequestDraft.currency);
    this.approvalForm.get('subject').setValue(approvalRequestDraft.subject);
    this.approvalForm.get('amount').setValue(approvalRequestDraft.amount);
  }

  reset() {
    this.setApprovalRequest(ApprovalRequestDraftSamples.empty);
  }

  businessTrip() {
    this.setApprovalRequest(ApprovalRequestDraftSamples.businessTrip);
  }

  advancedTraining() {
    this.setApprovalRequest(ApprovalRequestDraftSamples.advancedTraining);
  }

  sabbatical() {
    this.setApprovalRequest(ApprovalRequestDraftSamples.sabbatical);
  }
}
